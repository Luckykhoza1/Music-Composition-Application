module whitman.cs370proj.composer {
    // requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires transitive javafx.controls; //added to made it work on my PC

    opens whitman.cs370proj.composer to javafx.fxml;
    exports whitman.cs370proj.composer;
}