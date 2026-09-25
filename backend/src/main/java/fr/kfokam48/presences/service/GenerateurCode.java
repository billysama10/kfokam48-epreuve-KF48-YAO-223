package fr.kfokam48.presences.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/** Code de présence : 6 caractères en majuscules et chiffres (cahier des charges, section 7). */
@Component
public class GenerateurCode {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int LONGUEUR = 6;

    private final SecureRandom hasard = new SecureRandom();

    public String generer() {
        StringBuilder code = new StringBuilder(LONGUEUR);
        for (int i = 0; i < LONGUEUR; i++) {
            code.append(ALPHABET.charAt(hasard.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
