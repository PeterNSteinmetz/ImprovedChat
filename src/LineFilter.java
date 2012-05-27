/**
 * Interface to filter lines being sent to a Tab for display.
 * 
 * @author peter
 */

import java.util.List;
import java.util.regex.Pattern;

public interface LineFilter {

  public boolean isValid(String line);
  public boolean isIgnored(String line);
  public List<Pattern> getTrackPatterns();
  public void addTrackPattern(String regex);
  public void deleteTrackPattern(final int patInd);
  public List<Pattern> getIgnorePatterns();
  public void addIgnorePattern(String regex);
  public void deleteIgnorePattern(final int patInd);
  
}
