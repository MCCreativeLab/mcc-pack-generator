package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import de.verdox.vserializer.generic.Serializer;
import de.verdox.vserializer.generic.SerializerBuilder;

import java.util.Locale;
import java.util.Objects;

public record LanguageInfo(String identifier, String name, String region, boolean bidirectional) {

    static final Serializer<LanguageInfo> SERIALIZER = SerializerBuilder.createObjectToPrimitiveSerializer("language", LanguageInfo.class, Serializer.Primitive.STRING, LanguageInfo::identifier, s -> new LanguageInfo(s, "", "", false));

    public static LanguageInfo GERMAN = new LanguageInfo("de_de", "German", "Germany", false);
    public static LanguageInfo ENGLISH_AU = new LanguageInfo("en_au", "Australian English", "Australia", false);
    public static LanguageInfo ENGLISH_CA = new LanguageInfo("en_ca", "Canadian English", "Canada", false);
    public static LanguageInfo ENGLISH_GB = new LanguageInfo("en_gp", "British English", "Great Britain", false);
    public static LanguageInfo ENGLISH_NZ = new LanguageInfo("en_nu", "New Zealand English", "New Zealand", false);
    public static LanguageInfo ENGLISH_US = new LanguageInfo("en_us", "American English", "United States", false);


    public Locale toLocale(){
        String[] split = identifier.split("_");

        

        if(split.length != 2)
            return Locale.ENGLISH;
        if(split[0].equals(split[1]))
            return new Locale(split[0]);
        else
            return new Locale(split[0], split[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageInfo that = (LanguageInfo) o;
        return Objects.equals(identifier.toLowerCase(Locale.ROOT), that.identifier.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier.toLowerCase(Locale.ROOT));
    }
}
