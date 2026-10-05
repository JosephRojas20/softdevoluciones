package db.bootstrap;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V2_1__crear_cliente_demo extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String email = "cliente@softdv.com";

        // Si ya existe, conserva sus datos y contraseña.
        try (PreparedStatement consulta = context.getConnection()
                .prepareStatement(
                        "SELECT id FROM usuarios WHERE email = ?"
                )) {

            consulta.setString(1, email);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return;
                }
            }
        }

        // La contraseña se configurará de forma privada en Render.
        String password = System.getenv("BOOTSTRAP_CLIENT_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Configura BOOTSTRAP_CLIENT_PASSWORD "
                            + "para crear el cliente inicial"
            );
        }

        if (password.length() < 12 || password.length() > 72
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                        .length > 72) {
            throw new IllegalStateException(
                    "BOOTSTRAP_CLIENT_PASSWORD debe tener al menos "
                            + "12 caracteres y no superar 72 bytes"
            );
        }

        String passwordHash = new BCryptPasswordEncoder()
                .encode(password);

        String sql = """
                INSERT INTO usuarios (
                    nombre,
                    email,
                    password_hash,
                    rol,
                    activo
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement insercion = context.getConnection()
                .prepareStatement(sql)) {

            insercion.setString(1, "Cliente de prueba");
            insercion.setString(2, email);
            insercion.setString(3, passwordHash);
            insercion.setString(4, "CLIENTE");
            insercion.setBoolean(5, true);

            insercion.executeUpdate();
        }
    }
}