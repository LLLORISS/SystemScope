package nm.sc.systemscope.modules;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom implementation of JavaFX's ListView that adds additional functionality for managing and searching list items.
 * This class utilizes an ObservableList for automatic UI updates when the list changes.
 *
 * @param <T> the type of elements contained in this list view
 */
public class ScopeListView<T> extends ListView<T> {

    /**
     * Default constructor that initializes an empty observable list and sets it as the data source for this ListView.
     */
    public ScopeListView() {
        super();
        ObservableList<T> observableList = FXCollections.observableArrayList();
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
     */
    public static <T> List<T> searchItems(String searchInput, List<T> lst) {
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
}
