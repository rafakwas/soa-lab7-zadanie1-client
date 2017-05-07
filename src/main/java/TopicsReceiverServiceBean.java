//import javax.ejb.ActivationConfigProperty;
//import javax.ejb.MessageDriven;
//import javax.jms.Message;
//import javax.jms.MessageListener;
//
//@MessageDriven(name = "TopicsReceiverServiceEJB",activationConfig = {
//        @ActivationConfigProperty(propertyName =
//                "destinationType", propertyValue = "javax.jms.Topic"),
//        @ActivationConfigProperty(propertyName =
//                "destination", propertyValue = "java:/myJmsTest/MyTopic") })
//
//public class TopicsReceiverServiceBean implements MessageListener {
//
//
//    public TopicsReceiverServiceBean() {
//    }
//
//    @Override
//    public void onMessage(Message message) {
//
//        System.out.println("Zadanie 1 klient dostał wiadomość: " + message);
//
//    }
//}
