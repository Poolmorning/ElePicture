module com.example.elepicture {
    requires javafx.controls;
    requires javafx.fxml;



    opens com.example.elepicture to javafx.fxml;
    exports com.example.elepicture;
    exports com.example.elepicture.utils;
    opens com.example.elepicture.utils to javafx.fxml;
}