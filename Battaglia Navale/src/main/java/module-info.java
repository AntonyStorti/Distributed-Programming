module unisa.distributedprogramming {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Opening the unisa.distributedprogramming.client package for reflection via FXMLLoader
    opens unisa.distributedprogramming.client to javafx.fxml;

    // Export the base package and client package to necessary modules
    exports unisa.distributedprogramming;
    exports unisa.distributedprogramming.client to javafx.fxml, javafx.graphics;

}
