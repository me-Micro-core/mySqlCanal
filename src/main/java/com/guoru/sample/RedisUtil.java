package com.guoru.sample;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

	// Redis������IP
	private static String ADDR = "10.1.2.190";

	// Redis�Ķ˿ں�
	private static int PORT = 6379;

	// ��������
	private static String AUTH = "admin";

	// ��������ʵ���������Ŀ��Ĭ��ֵΪ8��
	// �����ֵΪ-1�����ʾ�����ƣ����pool�Ѿ�������maxActive��jedisʵ�������ʱpool��״̬Ϊexhausted(�ľ�)��
	private static int MAX_ACTIVE = 1024;

	// ����һ��pool����ж��ٸ�״̬Ϊidle(���е�)��jedisʵ����Ĭ��ֵҲ��8��
	private static int MAX_IDLE = 200;

	// �ȴ��������ӵ����ʱ�䣬��λ���룬Ĭ��ֵΪ-1����ʾ������ʱ����������ȴ�ʱ�䣬��ֱ���׳�JedisConnectionException��
	private static int MAX_WAIT = 10000;

	// ����ʱ��
	protected static int  expireTime = 60 * 60 *24;
	
	// ���ӳ�
	protected static JedisPool pool;

	/**
	 * ��̬���룬ֻ�ڳ��ε���һ��
	 */
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		//���������
		config.setMaxTotal(MAX_ACTIVE);
		//������ʵ��
		config.setMaxIdle(MAX_IDLE);
		//��ʱʱ��
		config.setMaxWaitMillis(MAX_WAIT);
		//
		config.setTestOnBorrow(false);
		pool = new JedisPool(config, ADDR, PORT, 1000);
	}

	/**
	 * ��ȡjedisʵ��
	 */
	protected static synchronized Jedis getJedis() {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
		} catch (Exception e) {
			e.printStackTrace();
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		}
		return jedis;
	}

	/**
	 * �ͷ�jedis��Դ
	 * 
	 * @param jedis
	 * @param isBroken
	 */
	protected static void closeResource(Jedis jedis, boolean isBroken) {
		try {
			if (isBroken) {
				pool.returnBrokenResource(jedis);
			} else {
				pool.returnResource(jedis);
			}
		} catch (Exception e) {

		}
	}

	/**
	 *  �Ƿ����key
	 * 
	 * @param key
	 */
	public static boolean existKey(String key) {
		Jedis jedis = null;
		boolean isBroken = false;
		try {
			jedis = getJedis();
			jedis.select(0);
			return jedis.exists(key);
		} catch (Exception e) {
			isBroken = true;
		} finally {
			closeResource(jedis, isBroken);
		}
		return false;
	}

	/**
	 *  ɾ��key
	 * 
	 * @param key
	 */
	public static void delKey(String key) {
		Jedis jedis = null;
		boolean isBroken = false;
		try {
			jedis = getJedis();
			jedis.select(0);
			jedis.del(key);
		} catch (Exception e) {
			isBroken = true;
		} finally {
			closeResource(jedis, isBroken);
		}
	}

	/**
	 *  ȡ��key��ֵ
	 * 
	 * @param key
	 */
	public static String stringGet(String key) {
		Jedis jedis = null;
		boolean isBroken = false;
		String lastVal = null;
		try {
			jedis = getJedis();
			jedis.select(0);
			lastVal = jedis.get(key);
			jedis.expire(key, expireTime);
		} catch (Exception e) {
			isBroken = true;
		} finally {
			closeResource(jedis, isBroken);
		}
		return lastVal;
	}

	/**
	 *  ���string����
	 * 
	 * @param key
	 * @param value
	 */
	public static String stringSet(String key, String value) {
		Jedis jedis = null;
		boolean isBroken = false;
		String lastVal = null;
		try {
			jedis = getJedis();
			jedis.select(0);
			lastVal = jedis.set(key, value);
			jedis.expire(key, expireTime);
		} catch (Exception e) {
			e.printStackTrace();
			isBroken = true;
		} finally {
			closeResource(jedis, isBroken);
		}
		return lastVal;
	}

	/**
	 *  ���hash����
	 * 
	 * @param key
	 * @param field
	 * @param value
	 */
	public static void hashSet(String key, String field, String value) {
		boolean isBroken = false;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			if (jedis != null) {
				jedis.select(0);
				jedis.hset(key, field, value);
				jedis.expire(key, expireTime);
			}
		} catch (Exception e) {
			isBroken = true;
		} finally {
			closeResource(jedis, isBroken);
		}
	}

}

