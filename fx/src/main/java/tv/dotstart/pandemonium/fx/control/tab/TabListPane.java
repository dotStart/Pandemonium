/*
 * Copyright 2017 Johannes Donath <me@dotstart.tv>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.pandemonium.fx.control.tab;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.DefaultProperty;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

/**
 * @author <a href="mailto:me@dotstart.tv">Johannes Donath</a>
 */
@DefaultProperty("tabs")
public class TabListPane extends HBox implements Initializable {
    private final ObjectProperty<Tab> activeTab = new SimpleObjectProperty<>();
    private final ObservableList<Tab> tabs = FXCollections.observableArrayList();

    // <editor-fold desc="FXML">
    @FXML
    private ListView<Tab> tabList;
    @FXML
    private StackPane contentPane;
    // </editor-fold>

    public TabListPane() {
        FXMLLoader loader = new FXMLLoader();

        loader.setCharset(StandardCharsets.UTF_8);
        loader.setController(this);
        loader.setRoot(this);

        try (InputStream inputStream = this.getClass().getResourceAsStream("TabListPane.fxml")) {
            loader.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load TabListPane control: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.activeTab.bind(this.tabList.getSelectionModel().selectedItemProperty());

        this.tabList.setItems(this.tabs);
        this.tabList.setCellFactory(new Callback<ListView<Tab>, ListCell<Tab>>() {
            @Override
            public ListCell<Tab> call(ListView<Tab> param) {
                return new ListCell<Tab>() {
                    @Override
                    protected void updateItem(Tab item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            this.setGraphic(null);
                            return;
                        }

                        Label titleLabel = new Label();
                        titleLabel.textProperty().bind(item.titleProperty());
                        this.setGraphic(titleLabel);
                    }
                };
            }
        });

        this.activeTab.addListener(this::onTabSelected);
        this.tabs.addListener(this::onTabsInvalidated);
    }

    /**
     * Handles tab selections.
     */
    private void onTabSelected(@Nonnull Observable observable) {
        Tab active = this.getActiveTab();

        this.contentPane.getChildren().clear();

        if (active == null) {
            return;
        }

        this.contentPane.getChildren().add(active);
    }

    /**
     * Handles tab list invalidation.
     */
    private void onTabsInvalidated(@Nonnull Observable observable) {
        Tab active = this.getActiveTab();

        if (active != null && !this.tabs.contains(active)) {
            this.tabList.getSelectionModel().clearSelection();
            active = null;
        }

        if (active == null && !this.tabs.isEmpty()) {
            this.tabList.getSelectionModel().select(0);
        }
    }

    // <editor-fold desc="Getters & Setters">
    @Nullable
    public Tab getActiveTab() {
        return this.activeTab.get();
    }

    @Nonnull
    public ReadOnlyObjectProperty<Tab> activeTabProperty() {
        return this.activeTab;
    }

    @Nonnull
    public ObservableList<Tab> getTabs() {
        return this.tabs;
    }
    // </editor-fold>
}
