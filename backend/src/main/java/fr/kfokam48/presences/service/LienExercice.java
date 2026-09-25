package fr.kfokam48.presences.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** RG14 : le lien d'un exercice est une URL absolue en http ou https (500 caractères au plus, D2). */
final class LienExercice {

    static final int LONGUEUR_MAX = 500;

    private LienExercice() {
    }

    static boolean valide(String lien) {
        if (lien == null || lien.isBlank() || lien.length() > LONGUEUR_MAX) {
            return false;
        }
        try {
            URI uri = new URI(lien.trim());
            String schema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            return (schema.equals("http") || schema.equals("https")) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
