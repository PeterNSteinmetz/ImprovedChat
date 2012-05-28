import java.util.ArrayList;
import java.util.Iterator;
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

	Pattern prefixPattern;
	ArrayList<Pattern> allowPatterns;
	
	/**
	 * Build for a regex string.
	 */
	public FixedRulesFilter() {
		allowPatterns = new ArrayList<Pattern>();
	}
	
	/**
	 * Determine if line is valid by whether it contains
	 * the prefix, and if it does, it it contains one of the allowed 
	 * patterns.
	 */
	@Override
	public boolean isValid(String line) {
		if (prefixPattern.matcher(line).find()) {
			Iterator<Pattern> patIter = allowPatterns.iterator();
			while (patIter.hasNext()) {
				if (patIter.next().matcher(line).find()) return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean isIgnored(String line) {
		return !isValid(line);
	}
	
	/**
	 * Set the prefix pattern, which if matched, requires matching the 
	 * allowed patterns.
	 */
	public void setPrefixPattern(String prePat) {
		System.out.println("prePat=/"+prePat+"/");
		try {
			Pattern pat = Pattern.compile(prePat);
			prefixPattern = pat;
		} catch (PatternSyntaxException pse) {
			; // don't crash
		}
	}
	
	/**
	 * Add an allow pattern which will be checked if the prefix matches.
	 */
	public void addAllowPattern(String allowPat) {
		try {
			Pattern pat = Pattern.compile(allowPat);
			allowPatterns.add(pat);
		} catch (PatternSyntaxException pse) {
			; // don't crash
		}
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
