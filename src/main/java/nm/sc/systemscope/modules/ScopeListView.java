package nm.sc.systemscope.modules;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom implementation of JavaFX's ListView that adds additional functionality for managing and searching list items.
 * This class utilizes an ObservableList for automatic UI updates when the list changes.
 *
 * @param <T> the type of elements contained in this list view
 */
public class ScopeListView<T> extends ListView<T> {

    private ObservableList<T> observableList;

    /**
     * Default constructor that initializes an empty observable list and sets it as the data source for this ListView.
     */
    public ScopeListView() {
        super();
        this.observableList = FXCollections.observableArrayList();
        this.setItems(observableList);
    }

    /**
     * Constructor that accepts an existing ObservableList to initialize this ListView.
     *
     * @param observableList the initial ObservableList for this ListView
     */
    public ScopeListView(ObservableList<T> observableList) {
        super();
        this.observableList = observableList;
        this.setItems(observableList);
    }

    /**
     * Searches for items in the given list that match the provided search input.
     * The search is case-insensitive and matches any item whose string representation contains the search input.
     *
     * @param searchInput the input string used for filtering items
     * @param lst the list of items to search
     * @param <T> the type of elements in the list
     * @return a list of items that match the search input; if the search input is null or empty, returns the original list
     * @throws IOException if an I/O error occurs (currently not utilized, can be removed if unnecessary)
     */
    public static <T> List<T> searchItems(String searchInput, List<T> lst) throws IOException {
        List<T> filtered = new ArrayList<>();

        if (searchInput == null || searchInput.trim().isEmpty()) {
            return lst;
        }

        for (T item : lst) {
            if (item != null && item.toString().toLowerCase().contains(searchInput.toLowerCase())) {
                filtered.add(item);
            }
        }

        return filtered;
    }

    /**
     * Updates the ListView to display only the items that match the provided search input.
     * The search is performed on the current items in the list.
     *
     * @param searchInput the input string used for filtering items
     * @throws IOException if an I/O error occurs during the search (currently not utilized, can be removed if unnecessary)
     */
    public void updateListView(String searchInput) throws IOException {
        List<T> searchResults = searchItems(searchInput, new ArrayList<>(observableList));
        observableList.setAll(searchResults);
    }

    /**
     * Adds an item to the ObservableList and updates the ListView.
     *
     * @param item the item to be added
     */
    public void addItem(T item) {
        observableList.add(item);
    }

    /**
     * Removes an item from the ObservableList and updates the ListView.
     *
     * @param item the item to be removed
     */
    public void removeItem(T item) {
        observableList.remove(item);
    }

    /**
     * Replaces the current items in the ObservableList with a new list of items.
     *
     * @param items the new list of items to set
     */
    public void setItemsList(List<T> items) {
        observableList.setAll(items);
    }

    /**
     * Retrieves the underlying ObservableList used by this ListView.
     *
     * @return the ObservableList associated with this ListView
     */
    public ObservableList<T> getObservableList() {
        return observableList;
    }
}
