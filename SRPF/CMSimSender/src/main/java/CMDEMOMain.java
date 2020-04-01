
import CM.CMAPI.CME_MessageType;
import CM.CMAPI.CME_TransferMode;
import CM.CMAPI.CM_Envelope;
import CM.CMAPI.CM_Exception;
import CM.CMAPI.CM_File;
import CM.CMAPI.CM_Message;
import CM.CMAPI.CM_Services;


class Runner implements Runnable{
	
	private CM_Services serv;
	private String sender;
	private String [] receivers;
	private String attachment;
	private String msgClass;
	private String msgType;

	public  Runner(CM_Services s, String sender, String [] receivers, String attachment, String msgClass, String msgType) {
		serv=s;
		this.sender=sender;
		this.receivers=receivers;
		this.attachment = attachment;
		this.msgClass = msgClass;
		this.msgType = msgType;
	}

	@Override
	public void run() {
		CM_Envelope envelop = new CM_Envelope();
		
		
		envelop.SetSender(sender);
		envelop.SetReceivers(receivers);
		CM_Message my_msg = new CM_Message();
		my_msg.SetClass(msgClass);
		CM_File[] attachments = new CM_File[1];
		CM_File attach = new CM_File();
		attach.SetName(attachment);
		attach.SetClass(msgClass);
		attachments[0] = attach;
		if(!attachment.equals("NULL"))
		{
			my_msg.SetAttachments(attachments);
		}
		
		if (msgType.equals("twoway")) {
			envelop.SetTransferMode(CME_TransferMode.CMC_TwoWay);
		}
		else {
			envelop.SetTransferMode(CME_TransferMode.CMC_OneWay);
		}
			
		
		String testo = new String("Message from thread " + Thread.currentThread().getId());
		my_msg.SetBody(testo.getBytes().length, testo.getBytes());
		my_msg.SetEnvelope(envelop);
		
		my_msg.SetType(CME_MessageType.CMC_RequestMessage);
		
		my_msg.SetTransactionID((int)Thread.currentThread().getId());
		
		
		
		CM_Message response;
		try {
			if (msgType.equals("twoway")) {
				response = serv.SendReceiveMessage(my_msg);
				String res = new String(response.GetBody());
			
				//System.out.println("Response:");
		
				//System.out.println(res);
			
				CM_File[] attachmentsArray = response.GetAttachments();
				if (attachmentsArray != null && attachmentsArray.length > 0) {
					//System.out.println("Attachment saved in: " + response.GetAttachments()[0].GetName());
				}
			}
			else {
				serv.SendMessage(my_msg);
				//System.out.println("Message sent");
			}
		} catch (CM_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}

public class CMDEMOMain {

	/**
	 * 
	 * @param args[0] = sender;  args[1] = receiver; args[2] = basket; args[3] = attachment; args[4] = msg class; args[5] = oneway|twoway
	 * Es.: IDUGS:S-IM:CDMFEAS IDUGS:S-RPF:FE /home/sim/cosmo2/attachment /home/sim/cosmo2/attachment/feas.xml FeasibilityAnalysis twoway
	 */
	public static void main(String[] args) {
		if (args.length != 8) {
			//System.out.println("args[0] = sender;  args[1] = receiver; args[2] = basket; args[3] = attachment;"+
		" args[4] = msg class; args[5] = oneway|twoway; args[6] = number of concurrent request ; args[7] = interlive time in millisec");
			System.exit(-1);
		}
		CM_Services serv = new CM_Services(args[0]);
		
		int number_of_conurrent_request=1;
		long interlive_time=250;
		try{
			number_of_conurrent_request= Integer.valueOf(args[6]);
			interlive_time = Long.valueOf(args[7]);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		
		try {
			serv.RegisterInBasket(args[4], args[2]);
			
			
			String [] receivers = new String[1];
			
			receivers[0]=args[1];
			
			//Runner run1 = new Runner(serv,args[0],receivers, args[3], args[4], args[5]);
			//Runner run2 = new Runner(serv,args[0],receivers, args[3], args[4]);
			
			Thread[] threads = new Thread[number_of_conurrent_request];
			
			for(int i =0; i< number_of_conurrent_request;i++)
			{
				Thread t1 = new Thread(new Runner(serv,args[0],receivers, args[3], args[4], args[5]));
				threads[i]=t1;
				t1.start();
				try {
					Thread.sleep(interlive_time);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					System.err.println(e1.getMessage());
				}
				
			}
			
			for(int j=0;j<number_of_conurrent_request;j++){
				try {
					threads[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.err.println(e.getMessage());
				}
			}
			
		
			
			
			
			
			}catch (CM_Exception e) {
				System.err.println(e.getMessage());
			
			
		}finally {
			try {
				serv.UnregisterInBasket(args[4]);
			} catch (CM_Exception e) {
				
				System.err.println(e.getMessage());
			}
		}

	}

}
