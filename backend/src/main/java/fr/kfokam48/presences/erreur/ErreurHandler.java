package fr.kfokam48.presences.erreur;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Gestion centralisée des erreurs (B4) : toute réponse d'erreur a la forme
 * { "code": "...", "message": "..." }, jamais de stack trace.
 */
@RestControllerAdvice
public class ErreurHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ErreurHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErreurDto> metier(ApiException e) {
        return reponse(e.getStatut(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurDto> validation(MethodArgumentNotValidException e) {
        String champs = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "Requête invalide : " + champs + ".");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErreurDto> requeteIllisible(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "Requête invalide ou incomplète.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErreurDto> routeInconnue(NoResourceFoundException e) {
        return reponse(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE", "Cette adresse n'existe pas.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErreurDto> methode(HttpRequestMethodNotSupportedException e) {
        return reponse(HttpStatus.METHOD_NOT_ALLOWED, "METHODE_NON_AUTORISEE", "Méthode HTTP non autorisée ici.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErreurDto> typeMedia(HttpMediaTypeNotSupportedException e) {
        return reponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TYPE_NON_SUPPORTE", "Le corps doit être en JSON.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurDto> inattendue(Exception e) {
        LOG.error("Erreur inattendue", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE", "Une erreur inattendue est survenue.");
    }

    private static ResponseEntity<ErreurDto> reponse(HttpStatus statut, String code, String message) {
        return ResponseEntity.status(statut).body(new ErreurDto(code, message));
    }
}
