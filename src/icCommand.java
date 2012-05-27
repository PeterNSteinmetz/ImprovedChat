/**
 * Abstract base of classes which implement commands which can be executed by the user.
 *
 */
public abstract class icCommand {

    public String usage;
    public String desc;
    public String success;


    public icCommand(String description, String usage, String success) {
        this.desc = description;
        this.usage = usage;
        this.success = success;
    }

    public abstract boolean process(String[] var1);
}