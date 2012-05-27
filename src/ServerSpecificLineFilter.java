import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * LineFilter which filters depending on the current server name.
 * @author peter
 *
 */
public class ServerSpecificLineFilter implements LineFilter {
		
	private HashMap<String,LineFilter> nameFilterMap;
	
	public ServerSpecificLineFilter() {
		nameFilterMap = new HashMap<String,LineFilter>();
	}
	
	/**
	 * Add a LineFilter for a server.
	 */
	public void addServerLineFilter(String serverName,LineFilter lf) {
		nameFilterMap.put(serverName, lf);
	}
 
	/**
	 * Get LineFilter for a server name.
	 * @param serverName
	 * @return LineFilter or null if name not registered.
	 */
	private LineFilter getLineFilter(String serverName) {
		return nameFilterMap.get(serverName);
	}
	
	/**
	 * Get LineFilter for current server.
	 * @return LineFilter or null if current server isn't registered.
	 */
	private LineFilter getCurrentServerFilter() {
		return getLineFilter(ImprovedChat.getCurrentServer().name);
	}
	
	@Override
	public boolean isValid(String line) {
		LineFilter lfilt = getCurrentServerFilter();
		return lfilt.isValid(line);
	}
	
	@Override
	public boolean isIgnored(String line) {
		LineFilter lfilt = getCurrentServerFilter();
		return lfilt.isIgnored(line);
	}
	
	@Override
	public List<Pattern> getTrackPatterns() {
		LineFilter lfilt = getCurrentServerFilter();
		return lfilt.getTrackPatterns();
	}
	
	@Override
	public void addTrackPattern(String regex) {
		LineFilter lfilt = getCurrentServerFilter();
		lfilt.addTrackPattern(regex);
	}
	
	@Override
	public void deleteTrackPattern(int patInd) {
		LineFilter lfilt = getCurrentServerFilter();
		lfilt.deleteTrackPattern(patInd);
	}
	
	@Override
	public List<Pattern> getIgnorePatterns() {
		LineFilter lfilt = getCurrentServerFilter();
		return lfilt.getIgnorePatterns();
	}
	
	@Override
	public void addIgnorePattern(String regex) {
		LineFilter lfilt = getCurrentServerFilter();
		lfilt.addIgnorePattern(regex);
	}
	
	@Override
	public void deleteIgnorePattern(int patInd) {
		LineFilter lfilt = getCurrentServerFilter();
		lfilt.deleteIgnorePattern(patInd);
	}

}
