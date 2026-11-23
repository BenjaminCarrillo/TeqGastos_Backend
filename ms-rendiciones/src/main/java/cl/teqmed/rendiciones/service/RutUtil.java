package cl.teqmed.rendiciones.service;

// RUT chileno: misma lógica que GastoValidator en la app (módulo 11)
public final class RutUtil {

    private RutUtil() { }

    public static boolean esValido(String rut) {
        String limpio = limpiar(rut);
        if (limpio.length() < 8 || limpio.length() > 9) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        if (!cuerpo.chars().allMatch(Character::isDigit)) return false;

        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {     // de derecha a izquierda
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }

    // "76.543.210-3" → "76543210-3"
    public static String normalizar(String rut) {
        String limpio = limpiar(rut);
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    private static String limpiar(String rut) {
        return rut == null ? "" : rut.replace(".", "").replace("-", "").trim().toUpperCase();
    }
}
