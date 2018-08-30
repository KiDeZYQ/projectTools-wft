package com.rampage.learning.activemq;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 简单的队列消费者
 * 
 * @author ziyuqi
 *
 */
public class SimpleConsumer {
	public static void main(String[] args) {
		// STEP1: 创建连接工厂
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
		        ActiveMQConnection.DEFAULT_PASSWORD, ActiveMQConnection.DEFAULT_BROKER_URL);

		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageConsumer consumer = null;
		try {
			// STEP2: 从连接工厂得到连接并且启动连接
			connection = connectionFactory.createConnection();
			connection.setClientID("5");		// 持久化订阅必须指定clientId,用来指定订阅的通道 同时间只能同时存在一个同clientId的连接，否则会报通道错误
			connection.start();

			// STEP3: 获取会话
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// STEP4: 创建目标队列
			destination = session.createQueue("KiDe-Demo");
			// topicDestination = session.createTopic("KiDe-Demo");
			
			// STEP5: 创建消费者
			consumer = session.createConsumer(destination);
			// topicConsumer = session.createConsumer(topicDestination);
			// 第二个参数表示用户名称，针对同一主题注册的唯一性是由clientId和username一起决定的 只有第二次两个参数完全一致的情况，才能获取topic中已经发布的消息
			TopicSubscriber topicSubscriber = session.createDurableSubscriber(session.createTopic("KiDe-topic-Demo"), "1");	
			
			
			// STEP6: 设置消息接收者接收消息 也可以通过死循环接收消息
			/*while (true) {
				TextMessage textMessage = (TextMessage) consumer.receive(1000);
				System.out.println(textMessage.getText());
			}*/
			consumer.setMessageListener(new MessageListener() {
				
				@Override
				public void onMessage(Message paramMessage) {
					TextMessage message = (TextMessage) paramMessage;
					try {
						System.out.println("消费者接收到队列消息：" + message.getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
			/*topicConsumer.setMessageListener(new MessageListener() {
				
				@Override
				public void onMessage(Message paramMessage) {
					TextMessage message = (TextMessage) paramMessage;
					try {
						System.out.println("消费者接收到主题消息：" + message.getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});*/
			topicSubscriber.setMessageListener(new MessageListener() {
				
				@Override
				public void onMessage(Message paramMessage) {
					TextMessage message = (TextMessage) paramMessage;
					try {
						System.out.println("消费者接收到主题消息：" + message.getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
			TimeUnit.SECONDS.sleep(200);	// 睡眠20秒，使得客户端可以接收到对应消息
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
				}
			}
		}
	}
}
