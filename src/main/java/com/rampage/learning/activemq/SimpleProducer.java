package com.rampage.learning.activemq;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 简单的队列消息生产者
 * @author ziyuqi
 *
 */
public class SimpleProducer {
	public static void main(String[] args) {
		// STEP1: 得到连接工厂
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
		        ActiveMQConnection.DEFAULT_PASSWORD, ActiveMQConnection.DEFAULT_BROKER_URL);
		
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer producer = null;
		MessageProducer topicProducer = null;
		Destination topicDestination = null;
		try {
			// STEP2: 从连接工厂得到连接并且启动连接
			connection = connectionFactory.createConnection();
			connection.start();
			
			// STEP3: 获取会话
			/**
			 * 第一个参数表示是否开启事务：
			 * 当第一个参数为true的时候，会忽略第二个参数，无论第二个参数为啥，都需要显示调用 session.commit() 消息才会提交到MQ
			 * 当第一个参数为false的时候，第二个参数不能为：Session.SESSION_TRANSACTED。 且当第二个参数为其他合法值时，都不需要调用 session.commit()，消息都会发送到MQ
			 * 第二个参数表示当未开启事务的时候，消费者或者客户端在什么时候发送确认消息
			 */
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			// STEP4: 创建目标队列、主题 
			/**
			 * 队列和主题的区别在于：
			 * 1、 队列是点对点的，队列中的消息只会被消费一次
			 * 2、 主题类似于广播机制，只要订阅了该主题的消费者都可以对该消息进行消费
			 * 3、 一般来说如果生产者在消费者启动之前创建了主题，那么消费者启动后接收不到主题。
			 */
			destination = session.createQueue("KiDe-Demo");
			topicDestination = session.createTopic("KiDe-topic-Demo");
			
			// STEP5: 创建消息生产者
			producer = session.createProducer(destination);
			topicProducer = session.createProducer(topicDestination);
			
			/**
			 * 参数表示生产者发送的消息是否进行持久化
			 */
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);		// 设置不持久化
			// topicProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);		// 设置不持久化
			/**
			 * 持久化的概念是针对activeMQ重启而言的
			 * 如果设置为持久化，则当activeMQ重启后，原来没有被消费的消息仍然存在，可以被新加入的consumer消费
			 * 如果设置为非持久化，即使消息没有被消费，重启之后就会丢失
			 */
			topicProducer.setDeliveryMode(DeliveryMode.PERSISTENT);		
			
			
			// STEP6: 发送消息
			for (int i=0; i<20; i++) {
				TextMessage message = session.createTextMessage("Producer message:" + i);
				producer.send(message);
				topicProducer.send(message);
			}
			
			// STEP7: 如果开启了事务 ，此时需要调用session提交操作
			// session.commit();
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
