package org.notima.fortnox.command;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.entities3.Account;
import org.notima.fortnox.command.completer.ConfigFortnoxAccountCompleter;
import org.notima.generic.ifacebusinessobjects.BusinessObjectFactory;

@Command(scope = "fortnox", name = "config-fortnox-account", description = "Configure an account in the chart of accounts.")
@Service
public class ConfigFortnoxAccount extends FortnoxCommand implements Action {

	public static final String CONF_ENABLED = "enabled";
	
	public static String[] configs = new String[] {
			CONF_ENABLED
	};
	
	
	@SuppressWarnings("rawtypes")
	@Reference
	private List<BusinessObjectFactory> bofs;
	
	@Reference 
	Session sess;
	
	@Argument(index = 0, name = "orgNo", description ="The orgno of the client", required = true, multiValued = false)
	private String orgNo = "";

	@Argument(index = 1, name = "accountNo", description ="The account number to configure.", required = true, multiValued = false)
	private String accountNo = "";
	
    @Argument(index = 2, name = "configuration parameter", description = "What to configure", required = true, multiValued = false)
    @Completion(ConfigFortnoxAccountCompleter.class)
    String key;

    @Argument(index = 3, name = "value", description = "Value provided to configuration. If omitted, value is set to null", required = false, multiValued = false)
    String value;
	
	@Override
	public Object execute() throws Exception {

		// Make sure key is recognized
		boolean keyOk = false;
		for (String c : configs) {
			if (c.equalsIgnoreCase(key)) {
				keyOk = true;
				break;
			}
		}
		
		if (!keyOk) {
			sess.getConsole().println("Configuration parameter '" + key + "' not recognized");
			return null;
		}

		Boolean toggle = null;
		try {
			toggle = value==null ? false : Boolean.parseBoolean(value);
		} catch (Exception e) {
		}
		
		FortnoxClient3 fc = getFortnoxClient(bofs, orgNo);
		
		int yearId = fc.getFinancialYear(null).getId();

		Account acct = fc.getAccount(yearId, Integer.parseInt(accountNo));
		
		if (CONF_ENABLED.equalsIgnoreCase(key)) {
			if (toggle!=null) {
				if ((acct.getActive() && toggle==false) || 
						(!acct.getActive() && toggle==true)) {
				
					acct.setActive(toggle);
					fc.updateAccount(yearId, acct);
					sess.getConsole().println("Enabled set to " + toggle);
				} else {
					sess.getConsole().println("No change.");
				}
				
			}
		}	
		
		return null;
	}
	
}