package teacher;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.IntConsumer;

/** The "Subject List / Section List / Student List" card: title, search, filter button, table. */
final class ListCard extends VBox {
    final TextField search = new TextField();
    final Button filter = new Button();
    private final VBox body = new VBox();
    private final double[] widths;

    ListCard(String title, String subtitle, String searchPrompt, double[] widths, String... headers) {
        this.widths = widths;
        getStyleClass().add("card");
        Ui.roundClip(this, 14);

        VBox titles = new VBox(2, Ui.label(title, "card-title"), Ui.label(subtitle, "card-sub"));
        titles.setAlignment(Pos.CENTER_LEFT);

        search.setPromptText(searchPrompt);
        search.getStyleClass().add("search-field");
        HBox searchBox = new HBox(8, Ui.icon("search", 16), search);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefWidth(372);
        searchBox.setMinWidth(372);

        filter.setGraphic(Ui.icon("filter", 24));
        filter.getStyleClass().add("icon-button");

        HBox top = new HBox(16, titles, Ui.spacer(), searchBox, filter);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getStyleClass().add("list-top");

        GridPane head = grid(headers, true);
        head.getStyleClass().add("table-head");

        getChildren().addAll(top, head, body);
    }

    /** rows: every cell as text; the last cell is the cyan action/year link. */
    void setRows(List<String[]> rows, IntConsumer onClick) {
        body.getChildren().clear();
        if (rows.isEmpty()) {
            Label none = Ui.label("No results found.", "empty-note");
            none.setMaxWidth(Double.MAX_VALUE);
            none.setAlignment(Pos.CENTER);
            none.setPrefHeight(80);
            body.getChildren().add(none);
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            final int idx = i;
            GridPane row = grid(rows.get(i), false);
            row.getStyleClass().add("table-row");
            Ui.hand(row);
            row.setOnMouseClicked(e -> onClick.accept(idx));
            body.getChildren().add(row);
        }
    }

    private GridPane grid(String[] cells, boolean header) {
        GridPane g = new GridPane();
        g.setMinHeight(60);
        g.setPrefHeight(60);
        javafx.scene.layout.RowConstraints rc = new javafx.scene.layout.RowConstraints();
        rc.setPercentHeight(100);
        rc.setValignment(VPos.CENTER);
        g.getRowConstraints().add(rc);
        for (double w : widths) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(w);
            g.getColumnConstraints().add(c);
        }
        for (int i = 0; i < cells.length; i++) {
            boolean last = i == cells.length - 1;
            String text = (!header && last) ? cells[i] + "  \u2192" : cells[i];
            Label l = new Label(text);
            l.getStyleClass().add(header ? "th" : (last ? "link-cyan" : "td"));
            if (last) GridPane.setHalignment(l, HPos.RIGHT);
            GridPane.setValignment(l, VPos.CENTER);
            g.add(l, i, 0);
        }
        g.getStyleClass().add("grid-pad");
        return g;
    }
}
