import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class to apply a fixed set of rules, ignoring those set 
 * through the GUI.
 * 
 * @author peter
 *
 */
public class FixedRulesFilter implements LineFilter {

	Pattern mandatoryMatchPattern;
	
	/**
	 * Build for a regex string.
	 */
	public FixedRulesFilter(String regex) {
		try {
			mandatoryMatchPattern = Pattern.compile(regex);
		} catch (PatternSyntaxException pse) {
			;	// at least don't crash
		}
	}
	
	/**
	 * Determine if line is valid by checking if 
	 * contains a pattern.
	 */
	@Override
	public boolean isValid(String line) {
		return mandatoryMatchPattern.matcher(line).find();
	}

	@Override
	public boolean isIgnored(String line) {
		return !isValid(line);
	}

	@Override
	public List<Pattern> getTrackPatterns() {
		return null;
	}

	@Override
	public void addTrackPattern(String regex) {
		;
	}

	@Override
	public void deleteTrackPattern(int patInd) {
		;
	}

	@Override
	public List<Pattern> getIgnorePatterns() {
		return null;
	}

	@Override
	public void addIgnorePattern(String regex) {
		;
	}

	@Override
	public void deleteIgnorePattern(int patInd) {
		;
	}

}
