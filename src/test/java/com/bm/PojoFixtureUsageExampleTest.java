package com.bm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bm.ejb3data.bo.LineItem;
import com.bm.ejb3data.bo.Order;
import com.bm.testsuite.PoJoFixture;

/**
 * Shows the usage of the pojo fixture.
 * 
 */
public class PojoFixtureUsageExampleTest extends PoJoFixture {

	private static final Class<?>[] USED_ENTITIES = { Order.class,
			LineItem.class };

	public PojoFixtureUsageExampleTest() {
		super(USED_ENTITIES);
	}

	/**
	 * Delets all data. {@inheritDoc}
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		deleteAll(LineItem.class);
		deleteAll(Order.class);
	}

	/**
	 * Delets all data. {@inheritDoc}
	 */
	@Override
	public void tearDown() throws Exception {
		deleteAll(LineItem.class);
		deleteAll(Order.class);
		super.tearDown();
	}

	public void testToWriteComplexObjectGraph() {
		Order radomOrder = this.generateRandomInstance(Order.class);
		radomOrder.addPurchase(this.generateRandomInstance(LineItem.class));
		radomOrder.addPurchase(this.generateRandomInstance(LineItem.class));
		List<Order> complexObjectGraph = generateTestOrders();
		complexObjectGraph.add(radomOrder);

		// persist the graph and load it again
		List<Order> persisted = persist(complexObjectGraph);
		List<Order> allFromDB = findAll(Order.class);

		// assert the persisted graph and the loaded are equal
		assertCollectionsEqual(persisted, allFromDB);
	}

	public void testGetEntityManager() {
		assertNotNull(this.getEntityManager());
	}

	private List<Order> generateTestOrders() {
		final List<Order> orders = new ArrayList<Order>();
		Order order = new Order();
		order.setExpiration(new Date());
		order.addPurchase("Testprod1", 30, 30.34);
		order.addPurchase("Testprod2", 31, 31.34);
		order.addPurchase("Testprod3", 32, 32.34);
		order.addPurchase("Testprod4", 33, 33.34);
		orders.add(order);
		return orders;
	}

}
