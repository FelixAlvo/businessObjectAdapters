package org.notima.fortnox.command;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.notima.api.fortnox.FortnoxCredentialsProvider;
import org.notima.api.fortnox.clients.FortnoxCredentials;
import org.notima.businessobjects.adapter.fortnox.FileCredentialsProvider;
import org.notima.fortnox.command.completer.FortnoxTenantCompleter;

@Command(scope = "fortnox", name = "update-fortnox-credential", description = "Update active credential manually")
@Service
public class UpdateActiveCredential extends FortnoxCommand implements Action {

	@Reference 
	Session sess;
	
	@Argument(index = 0, name = "orgNo", description ="The orgno of the client", required = true, multiValued = false)
	@Completion(FortnoxTenantCompleter.class)	
	private String orgNo = "";

	@Argument(index = 1, name = "accessToken", description ="The access token", required = true, multiValued = false)
	private String accessToken = "";
	
	@Argument(index = 2, name = "refreshToken", description ="The refresh token", required = true, multiValued = false)
	private String refreshToken = "";
	
	@Argument(index = 3, name = "lastRefresh", description ="Seconds since epoch", required = true, multiValued = false)
	private Long lastRefresh;
	
	private FortnoxCredentialsProvider	credentialsProvider;
	private List<FortnoxCredentials>	credentials;
	
	private FortnoxCredentials			cred;
	
	@Override
	public Object execute() throws Exception {
		
		bf = this.getBusinessObjectFactoryForOrgNo(orgNo);

		if (bf==null) {
			sess.getConsole().println("No tenant found with orgNo [" + orgNo + "]");
			return null;
		}
		
		initCredentialsProvider();

		if(credentials == null) {
			sess.getConsole().println("No credentials found");
			return null;
		}
		
		convertToCredential();
		
		credentialsProvider.setCredentials(cred);
		
		return null;
	}
	
	private void convertToCredential() {
		
		cred = new FortnoxCredentials();
		cred.setAccessToken(accessToken);
		cred.setRefreshToken(refreshToken);
		cred.setLastRefresh(lastRefresh);
		
	}
	
	
	private void initCredentialsProvider() throws Exception {
		credentialsProvider = new FileCredentialsProvider(orgNo);
		credentials = credentialsProvider.getAllCredentials();
	}
	
	
}
