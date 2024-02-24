package ml.karmaconfigs.closedblockselevator.storage.custom;

public class ModelConfiguration {

    private final boolean prompt,force,advise;
    private final String url,hash;

    public ModelConfiguration(final boolean p, final boolean f, final boolean a, final String u, final String h) {
        prompt = p;
        force = f;
        advise = a;
        url = u;
        hash = h;
    }

    public boolean prompt() {
        return prompt;
    }

    public String downloadURL() {
        return url;
    }

    public byte[] hash() {
        return hash.getBytes();
    }

    public boolean force() {
        return force;
    }

    public boolean advise() {
        return advise;
    }
}
