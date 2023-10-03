package org.notima.fortnox.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.entities3.Invoice;
import org.notima.businessobjects.adapter.fortnox.FortnoxInvoiceModifyer;
import org.notima.fortnox.command.completer.FortnoxInvoicePropertyCompleter;
import org.notima.fortnox.command.completer.FortnoxTenantCompleter;

@Command(scope = "fortnox", name = "modify-fortnox-invoice", description = "Modify a specific invoice")
@Service
public class ModifyInvoice extends FortnoxCommand implements Action  {
	
	@Reference 
	Session sess;
	
	@Option(name = "--no-confirm", description = "Don't confirm anything. Default is to confirm", required = false, multiValued = false)
	private boolean noConfirm = false;

	@Argument(index = 0, name = "orgNo", description ="The orgno of the client", required = true, multiValued = false)
	@Completion(FortnoxTenantCompleter.class)	
	private String orgNo = "";

	@Argument(index = 1, name = "invoiceNo", description ="The invoice no", required = true, multiValued = false)
	private String invoiceNo;

    @Argument(index = 2, name = "property", description = "The property to modify", required = true, multiValued = false)
    @Completion(FortnoxInvoicePropertyCompleter.class)
    private String propertyToModify;

    @Argument(index = 3, name = "value", description = "The new value for the property", required = false, multiValued = false)
    private String newValue;

	private FortnoxClient3 fortnoxClient;
	private Invoice		   invoice;

    @Override
    public Object execute() throws Exception {

		Boolean everythingWorks = preperation();

		if (!everythingWorks){
			return null;
		}

		FortnoxInvoiceModifyer modifyer = new FortnoxInvoiceModifyer(null, propertyToModify, newValue, fortnoxClient, sess);
		modifyer.modifyInvoiceSingle(invoice);

		return null;

    }

	private Boolean preperation(){

        try{
            fortnoxClient = getFortnoxClient(orgNo);
        } catch (Exception e){
            e.printStackTrace();
        }
		if (fortnoxClient == null) {
			sess.getConsole().println("Can't get client for " + orgNo);
			return false;
		}


		try{
			invoice = fortnoxClient.getInvoice(invoiceNo);
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(invoice == null){
            sess.getConsole().println("Can't get invoice " + invoiceNo);
            return false;
        }


        String reply = "";
        try{
			String clientName = fortnoxClient.getCompanySetting().getName();
			reply = noConfirm ? "y" : sess.readLine("Do you want to modify invoice " + invoiceNo + " " + invoice.getCustomerName() + " for client " + clientName + "? (y/n) ", null);
        } catch (Exception e){
            e.printStackTrace();
        }
		if (!reply.equalsIgnoreCase("y")) {
		    sess.getConsole().println("Modification cancelled.");
		    return false;
	    } 
        return true;
    }
}
