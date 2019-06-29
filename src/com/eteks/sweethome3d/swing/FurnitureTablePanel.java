/*
 * FurniturePanel.java 19 juil. 2018
 *
 * Sweet Home 3D, Copyright (c) 2018 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.FurnitureView;

/**
 * A panel displaying home furniture table and other information like totals.
 * @author Emmanuel Puybaret
 */
public class FurnitureTablePanel extends JPanel implements FurnitureView, Printable {
  private JComponent          furnitureTable;
  private JLabel              totalPriceLabel;
  private JFormattedTextField totalPriceTextField;
  private JLabel              totalValueAddedTaxLabel;
  private JFormattedTextField totalValueAddedTaxTextField;
  private JLabel              totalPriceValueAddedTaxIncludedLabel;
  private JFormattedTextField totalPriceValueAddedTaxIncludedTextField;

  public FurnitureTablePanel(Home home, UserPreferences preferences,
                        FurnitureController controller) {
    super(new GridBagLayout());
    createComponents(home, preferences, controller);
    layoutComponents();
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final Home home,
                                final UserPreferences preferences,
                                FurnitureController controller) {
    this.furnitureTable = (JComponent)createFurnitureTable(home, preferences, controller);

    this.totalPriceLabel = new JLabel(preferences.getLocalizedString(
        FurnitureTablePanel.class, "totalPriceLabel.text"));
    this.totalPriceTextField = createTotalTextField();

    this.totalValueAddedTaxLabel = new JLabel(preferences.getLocalizedString(
        FurnitureTablePanel.class, "totalValueAddedTaxLabel.text"));
    this.totalValueAddedTaxTextField = createTotalTextField();

    // Create price Value Added Tax included label and its spinner bound to DEPTH controller property
    this.totalPriceValueAddedTaxIncludedLabel = new JLabel(preferences.getLocalizedString(
        FurnitureTablePanel.class, "totalPriceValueAddedTaxIncludedLabel.text"));
    this.totalPriceValueAddedTaxIncludedTextField = createTotalTextField();

    updateTotalsVisibility(preferences);
    updateTotals(home, preferences);

    // Add listener to update totals when furniture price changes
    final PropertyChangeListener furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (HomePieceOfFurniture.Property.PRICE.name().equals(ev.getPropertyName())
              || HomePieceOfFurniture.Property.VALUE_ADDED_TAX_PERCENTAGE.name().equals(ev.getPropertyName())
              || HomePieceOfFurniture.Property.CURRENCY.name().equals(ev.getPropertyName())) {
            updateTotals(home, preferences);
          }
        }
      };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      addPropertyChangeListener(piece, furnitureChangeListener);
    }
    home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture>() {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          HomePieceOfFurniture piece = ev.getItem();
          if (ev.getType() == CollectionEvent.Type.ADD) {
            addPropertyChangeListener(piece, furnitureChangeListener);
          } else {
            removePropertyChangeListener(piece, furnitureChangeListener);
          }
          updateTotals(home, preferences);
        }
      });

    UserPreferencesChangeListener preferencesListener = new UserPreferencesChangeListener(this, home);
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.CURRENCY, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.VALUE_ADDED_TAX_ENABLED, preferencesListener);
  }

  private void addPropertyChangeListener(HomePieceOfFurniture piece, PropertyChangeListener listener) {
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture child : ((HomeFurnitureGroup)piece).getFurniture()) {
        addPropertyChangeListener(child, listener);
      }
    } else {
      piece.addPropertyChangeListener(listener);
    }
  }

  private void removePropertyChangeListener(HomePieceOfFurniture piece, PropertyChangeListener listener) {
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture child : ((HomeFurnitureGroup)piece).getFurniture()) {
        removePropertyChangeListener(child, listener);
      }
    } else {
      piece.removePropertyChangeListener(listener);
    }
  }

  /**
   * Creates and returns the main furniture table displayed by this component.
   */
  protected FurnitureView createFurnitureTable(Home home, UserPreferences preferences, FurnitureController controller) {
    return new FurnitureTable(home, preferences, controller);
  }

  private JFormattedTextField createTotalTextField() {
    NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance();
    JFormattedTextField totalTextField = new JFormattedTextField(currencyFormat);
    totalTextField.setEditable(false);
    totalTextField.setFocusable(false);
    return totalTextField;
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  public static class UserPreferencesChangeListener implements PropertyChangeListener {
    private final WeakReference<FurnitureTablePanel> furnitureTablePanel;
    private final WeakReference<Home>                home;

    public UserPreferencesChangeListener(FurnitureTablePanel furnitureTotalPricePanel,
                                         Home home) {
      this.furnitureTablePanel = new WeakReference<FurnitureTablePanel>(furnitureTotalPricePanel);
      this.home = new WeakReference<Home>(home);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If furniture table was garbage collected, remove this listener from preferences
      FurnitureTablePanel furnitureTablePanel = this.furnitureTablePanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      UserPreferences.Property property = UserPreferences.Property.valueOf(ev.getPropertyName());
      if (furnitureTablePanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        switch (property) {
          case LANGUAGE :
          case CURRENCY :
            furnitureTablePanel.totalPriceLabel.setText(preferences.getLocalizedString(
                FurnitureTablePanel.class, "totalPriceLabel.text"));
            furnitureTablePanel.totalValueAddedTaxLabel.setText(
                preferences.getLocalizedString(FurnitureTablePanel.class, "totalValueAddedTaxLabel.text"));
            furnitureTablePanel.totalPriceValueAddedTaxIncludedLabel.setText(
                preferences.getLocalizedString(FurnitureTablePanel.class, "totalPriceValueAddedTaxIncludedLabel.text"));
            furnitureTablePanel.updateTotals(this.home.get(), preferences);
            // No break
          case VALUE_ADDED_TAX_ENABLED :
            furnitureTablePanel.updateTotalsVisibility(preferences);
            break;
        }
      }
    }
  }

  /**
   * Updates visibility of the total text fields.
   */
  private void updateTotalsVisibility(UserPreferences preferences) {
    this.totalPriceLabel.setVisible(preferences.getCurrency() != null);
    this.totalPriceTextField.setVisible(preferences.getCurrency() != null);
    this.totalValueAddedTaxLabel.setVisible(preferences.isValueAddedTaxEnabled());
    this.totalValueAddedTaxTextField.setVisible(preferences.isValueAddedTaxEnabled());
    this.totalPriceValueAddedTaxIncludedLabel.setVisible(preferences.isValueAddedTaxEnabled());
    this.totalPriceValueAddedTaxIncludedTextField.setVisible(preferences.isValueAddedTaxEnabled());
  }

  /**
   * Updates the values shown by total text fields.
   */
  private void updateTotals(Home home, UserPreferences preferences) {
    List<HomePieceOfFurniture> furniture = home.getFurniture();
    if (furniture.size() > 0) {
      BigDecimal totalPrice = new BigDecimal("0");
      BigDecimal totalValueAddedTax = new BigDecimal("0");
      BigDecimal totalPriceValueAddedTaxIncluded = new BigDecimal("0");
      FurnitureFilter furnitureFilter = getFurnitureFilter();
      String currencyCode = null;
      boolean currencySet = false;
      for (HomePieceOfFurniture piece : home.getFurniture()) {
        if (furnitureFilter == null || furnitureFilter.include(home, piece)) {
          BigDecimal price = piece.getPrice();
          if (price != null) {
            if (!currencySet) {
              currencySet = true;
              currencyCode = piece.getCurrency();
            } else if ((currencyCode != null || piece.getCurrency() != null)
                       && (currencyCode == null || !currencyCode.equals(piece.getCurrency()))) {
              // Cancel sum if prices are not in the same currency
              this.totalPriceTextField.setValue(null);
              this.totalValueAddedTaxTextField.setValue(null);
              this.totalPriceValueAddedTaxIncludedTextField.setValue(null);
              return;
            }
            totalPrice = totalPrice.add(price);
            BigDecimal valueAddedTax = piece.getValueAddedTax();
            if (valueAddedTax != null) {
              totalValueAddedTax = totalValueAddedTax.add(valueAddedTax);
            }
            totalPriceValueAddedTaxIncluded = totalPriceValueAddedTaxIncluded.add(piece.getPriceValueAddedTaxIncluded());
          }
        }
      }

      NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance();
      if (currencyCode == null) {
        currencyCode = preferences.getCurrency();
      }
      if (currencyCode != null) {
        try {
          Currency currency = Currency.getInstance(currencyCode);
          currencyFormat.setCurrency(currency);
          currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        } catch (IllegalArgumentException ex) {
          // Ignore currency
        }
      }
      DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory(new NumberFormatter(currencyFormat));
      this.totalPriceTextField.setFormatterFactory(formatterFactory);
      this.totalValueAddedTaxTextField.setFormatterFactory(formatterFactory);
      this.totalPriceValueAddedTaxIncludedTextField.setFormatterFactory(formatterFactory);

      this.totalPriceTextField.setValue(totalPrice);
      this.totalValueAddedTaxTextField.setValue(totalValueAddedTax);
      this.totalPriceValueAddedTaxIncludedTextField.setValue(totalPriceValueAddedTaxIncluded);
    } else {
      this.totalPriceTextField.setValue(null);
      this.totalValueAddedTaxTextField.setValue(null);
      this.totalPriceValueAddedTaxIncludedTextField.setValue(null);
    }
    revalidate();
  }

  /**
   * Layouts the components displayed by this panel.
   */
  private void layoutComponents() {
    JScrollPane furnitureScrollPane = SwingTools.createScrollPane(this.furnitureTable);
    furnitureScrollPane.setMinimumSize(new Dimension());
    // Add a mouse listener that gives focus to furniture view when
    // user clicks in its viewport (tables don't spread vertically if their row count is too small)
    final JViewport viewport = furnitureScrollPane.getViewport();
    viewport.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            viewport.getView().requestFocusInWindow();
          }
        });

    // Set default traversal keys of furniture view to ignore tab key within the table
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    this.furnitureTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    this.furnitureTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

    SwingTools.installFocusBorder(this.furnitureTable);
    setFocusTraversalPolicyProvider(false);
    setMinimumSize(new Dimension());

    add(furnitureScrollPane, new GridBagConstraints(
        0, 0, 6, 1, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    int standardGap = Math.round(2 * SwingTools.getResolutionScale());
    add(this.totalPriceLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(standardGap, 2, 0, standardGap), 0, 0));
    add(this.totalPriceTextField, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));
    add(this.totalValueAddedTaxLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(standardGap, standardGap, 0, standardGap), 0, 0));
    add(this.totalValueAddedTaxTextField, new GridBagConstraints(
        3, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));
    add(this.totalPriceValueAddedTaxIncludedLabel, new GridBagConstraints(
        4, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(standardGap, standardGap, 0, standardGap), 0, 0));
    add(this.totalPriceValueAddedTaxIncludedTextField, new GridBagConstraints(
        5, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));
  }

  @Override
  public void doLayout() {
    super.doLayout();
    GridBagLayout layout = (GridBagLayout)getLayout();
    int [][] dimensions = layout.getLayoutDimensions();
    if (dimensions.length > 1
        && this.totalValueAddedTaxLabel.isVisible()) {
      int [] widths = dimensions [0];
      GridBagConstraints constraints = layout.getConstraints(this.totalValueAddedTaxLabel);
      GridBagConstraints totalPriceLabelContraints = layout.getConstraints(this.totalPriceLabel);
      if (constraints.gridy == totalPriceLabelContraints.gridy) {
        // If the Total components are on the same row but not large enough to display their value
        if (widths [5] < this.totalPriceValueAddedTaxIncludedTextField.getPreferredSize().width) {
          // Place the Total components on 3 rows
          constraints.gridx = totalPriceLabelContraints.gridx;
          constraints.gridy = totalPriceLabelContraints.gridy + 1;
          layout.setConstraints(this.totalValueAddedTaxLabel, constraints);
          constraints = layout.getConstraints(this.totalValueAddedTaxTextField);
          constraints.gridx = totalPriceLabelContraints.gridx + 1;
          constraints.gridy = totalPriceLabelContraints.gridy + 1;
          layout.setConstraints(this.totalValueAddedTaxTextField, constraints);
          constraints = layout.getConstraints(this.totalPriceValueAddedTaxIncludedLabel);
          constraints.gridx = totalPriceLabelContraints.gridx;
          constraints.gridy = totalPriceLabelContraints.gridy + 2;
          layout.setConstraints(this.totalPriceValueAddedTaxIncludedLabel, constraints);
          constraints = layout.getConstraints(this.totalPriceValueAddedTaxIncludedTextField);
          constraints.gridx = totalPriceLabelContraints.gridx + 1;
          constraints.gridy = totalPriceLabelContraints.gridy + 2;
          layout.setConstraints(this.totalPriceValueAddedTaxIncludedTextField, constraints);
          super.doLayout();
        }
      } else {
        // Try to place the Total components on 1 row
        constraints.gridx = totalPriceLabelContraints.gridx + 2;
        constraints.gridy = totalPriceLabelContraints.gridy;
        layout.setConstraints(this.totalValueAddedTaxLabel, constraints);
        constraints = layout.getConstraints(this.totalValueAddedTaxTextField);
        constraints.gridx = totalPriceLabelContraints.gridx + 3;
        constraints.gridy = totalPriceLabelContraints.gridy;
        layout.setConstraints(this.totalValueAddedTaxTextField, constraints);
        constraints = layout.getConstraints(this.totalPriceValueAddedTaxIncludedLabel);
        constraints.gridx = totalPriceLabelContraints.gridx + 4;
        constraints.gridy = totalPriceLabelContraints.gridy;
        layout.setConstraints(this.totalPriceValueAddedTaxIncludedLabel, constraints);
        constraints = layout.getConstraints(this.totalPriceValueAddedTaxIncludedTextField);
        constraints.gridx = totalPriceLabelContraints.gridx + 5;
        constraints.gridy = totalPriceLabelContraints.gridy;
        layout.setConstraints(this.totalPriceValueAddedTaxIncludedTextField, constraints);
        // Compute again layout to check if the Total components can fit on one row
        doLayout();
      }
    }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if (this.furnitureTable instanceof Printable) {
      return ((Printable)this.furnitureTable).print(g, pageFormat, pageIndex);
    } else {
      throw new IllegalStateException("Embeded furniture view not printable");
    }
  }

  @Override
  public void setTransferHandler(TransferHandler newHandler) {
    this.furnitureTable.setTransferHandler(newHandler);
    ((JViewport)this.furnitureTable.getParent()).setTransferHandler(newHandler);
  }

  @Override
  public void setComponentPopupMenu(JPopupMenu popup) {
    this.furnitureTable.setComponentPopupMenu(popup);
    ((JViewport)this.furnitureTable.getParent()).setComponentPopupMenu(popup);
  }

  /**
   * Returns a copy of the furniture data for transfer purpose.
   */
  public Object createTransferData(DataType dataType) {
    return ((FurnitureView)this.furnitureTable).createTransferData(dataType);
  }

  /**
   * Returns <code>true</code> if the given format is CSV.
   */
  public boolean isFormatTypeSupported(FormatType formatType) {
    return ((FurnitureView)this.furnitureTable).isFormatTypeSupported(formatType);
  }

  /**
   * Writes in the given stream the content of the table at CSV format if this is the requested format.
   */
  public void exportData(OutputStream out, FormatType formatType, Properties settings) throws IOException {
    ((FurnitureView)this.furnitureTable).exportData(out, formatType, settings);
  }

  /**
   * Sets the filter applied to the furniture displayed by this component.
   */
  public void setFurnitureFilter(FurnitureView.FurnitureFilter filter) {
    ((FurnitureView)this.furnitureTable).setFurnitureFilter(filter);
  }

  /**
   * Returns the filter applied to the furniture displayed in this component.
   */
  public FurnitureView.FurnitureFilter getFurnitureFilter() {
    return ((FurnitureView)this.furnitureTable).getFurnitureFilter();
  }
}
