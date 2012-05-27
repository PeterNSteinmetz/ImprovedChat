import java.util.ArrayList;
import java.util.List;

/**
 * A single chat tab.
 * 
 */
public class Tab {

    public String name;
    public String prefix;
    public int width;
    public boolean blink = false;
    public boolean blinking = false;
    public int chatScroll = 0;

    public List<nt> e = new ArrayList<nt>();
    
    private LineFilter lineFilter;

    public Tab(String name) {
        this.name = name;
        this.prefix = "";
        this.width = ImprovedChat.minecraft.q.a(name);
        this.lineFilter = new DefaultLineFilter();
    }
    
    /**
     * Set LineFilter to use in determining which lines to display.
     * 
     * @param lineFilter
     */
    public void setLineFilter(LineFilter lf) {
    	lineFilter = lf;
    }
    
    /**
     * Retrieve current LineFilter in use.
     * @return lineFilter
     */
    public LineFilter getLineFilter() {
    	return lineFilter;
    }
    
    public boolean valid(String line) {
    	return lineFilter.isValid(line);
    }
    
    public boolean ignored(String line) {
    	return lineFilter.isIgnored(line);
    }
    
    public void track(String regex) {
    	lineFilter.addTrackPattern(regex);
    }
    
    public void ignore(String regex) {
    	lineFilter.addIgnorePattern(regex);
    }
    
    public void add(String l) {
        this.e.add(0, new nt(l));
        this.blinking = this.blink;
    }

    public void add(nt l) {
        // l.tabName = this.name;
        this.e.add(0, l);
        this.blinking = this.blink;
    }

    public void setName(String name) {
        this.name = name;
        this.width = ImprovedChat.minecraft.q.a(name);
    }
}