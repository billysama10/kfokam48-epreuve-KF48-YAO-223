package fr.kfokam48.presences.erreur;

import org.springframework.http.HttpStatus;

/**
 * Erreur métier levée par les services : porte le statut HTTP et le code stable
 * du catalogue de api/contrat.yaml. Traduite en { code, message } par ErreurHandler.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus statut;
    private final String code;

    public ApiException(HttpStatus statut, String code, String message) {
        super(message);
        this.statut = statut;
        this.code = code;
    }

    public static ApiException requeteInvalide(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ApiException introuvable(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }

    public static ApiException conflit(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    public HttpStatus getStatut() { return statut; }
    public String getCode() { return code; }
}
