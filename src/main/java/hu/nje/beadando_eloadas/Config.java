package hu.nje.beadando_eloadas;

import com.oanda.v20.account.AccountID;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private Config() {
    }

    private static final Dotenv DOTENV = Dotenv.configure()
            .directory("./")
            .load();

    public static final String URL =
            "https://api-fxpractice.oanda.com";

    public static final String TOKEN =
            getRequiredValue("OANDA_TOKEN");

    public static final AccountID ACCOUNTID =
            new AccountID(getRequiredValue("OANDA_ACCOUNT_ID"));

    private static String getRequiredValue(String key) {
        String value = DOTENV.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing value in .env: " + key
            );
        }

        return value;
    }
}