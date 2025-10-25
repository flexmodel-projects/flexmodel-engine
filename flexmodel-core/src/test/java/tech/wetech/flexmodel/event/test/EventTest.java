package tech.wetech.flexmodel.event.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.DataSourceProvider;
import tech.wetech.flexmodel.event.ChangedEvent;
import tech.wetech.flexmodel.event.EventListener;
import tech.wetech.flexmodel.event.EventPublisher;
import tech.wetech.flexmodel.event.PreChangeEvent;
import tech.wetech.flexmodel.event.impl.InsertedEvent;
import tech.wetech.flexmodel.event.impl.PreInsertEvent;
import tech.wetech.flexmodel.event.impl.PreUpdateEvent;
import tech.wetech.flexmodel.event.impl.SimpleEventPublisher;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 事件功能测试
 *
 * @author cjbi
 */
public class EventTest {

  private EventPublisher eventPublisher;
  private SessionFactory sessionFactory;
  private AtomicInteger preInsertCount;
  private AtomicInteger insertedCount;
  private AtomicReference<Object> lastInsertedData;

  @BeforeEach
  void setUp() {
    eventPublisher = new SimpleEventPublisher();
    preInsertCount = new AtomicInteger(0);
    insertedCount = new AtomicInteger(0);
    lastInsertedData = new AtomicReference<>();

    // 添加测试监听器
    eventPublisher.addListener(new EventListener() {
      @Override
      public void onPreChange(PreChangeEvent event) {
        if ("PRE_INSERT".equals(event.getEventType())) {
          preInsertCount.incrementAndGet();
        }
      }

      @Override
      public void onChanged(ChangedEvent event) {
        if ("INSERTED".equals(event.getEventType())) {
          insertedCount.incrementAndGet();
          lastInsertedData.set(event.getNewData());
        }
      }

      @Override
      public boolean supports(String eventType) {
        return "PRE_INSERT".equals(eventType) || "INSERTED".equals(eventType);
      }

      @Override
      public int getOrder() {
        return 100;
      }
    });

    // 创建SessionFactory（需要配置数据源）
    // sessionFactory = SessionFactory.builder()
    //     .setEventPublisher(eventPublisher)
    //     .setDefaultDataSourceProvider(createTestDataSource())
    //     .build();
  }

  @Test
  void testEventPublishing() {
    // 测试事件发布
    PreInsertEvent preEvent = new PreInsertEvent("test", "schema",
      Map.of("id", 1, "name", "test"), 1, "session1", null);

    eventPublisher.publishPreChangeEvent(preEvent);

    assertEquals(1, preInsertCount.get());
  }

  @Test
  void testChangedEventPublishing() {
    // 测试后置事件发布
    InsertedEvent changedEvent = new InsertedEvent("test", "schema",
      null, Map.of("id", 1, "name", "test"), 1, 1, true, null, "session1", null);

    eventPublisher.publishChangedEvent(changedEvent);

    assertEquals(1, insertedCount.get());
    assertNotNull(lastInsertedData.get());
  }

  @Test
  void testEventOrder() {
    AtomicInteger order = new AtomicInteger(0);

    // 添加多个监听器测试优先级
    eventPublisher.addListener(new EventListener() {
      @Override
      public void onPreChange(PreChangeEvent event) {
        order.set(2); // 低优先级
      }

      @Override
      public void onChanged(ChangedEvent event) {
        // 不处理
      }

      @Override
      public boolean supports(String eventType) {
        return "PRE_INSERT".equals(eventType);
      }

      @Override
      public int getOrder() {
        return 200; // 低优先级
      }
    });

    eventPublisher.addListener(new EventListener() {
      @Override
      public void onPreChange(PreChangeEvent event) {
        order.set(1); // 高优先级
      }

      @Override
      public void onChanged(ChangedEvent event) {
        // 不处理
      }

      @Override
      public boolean supports(String eventType) {
        return "PRE_INSERT".equals(eventType);
      }

      @Override
      public int getOrder() {
        return 50; // 高优先级
      }
    });

    PreInsertEvent preEvent = new PreInsertEvent("test", "schema",
      Map.of("id", 1), 1, "session1", null);

    eventPublisher.publishPreChangeEvent(preEvent);

    // 高优先级的监听器应该先执行
    assertEquals(2, order.get());
  }

  @Test
  void testEventSupports() {
    AtomicInteger supportedCount = new AtomicInteger(0);

    eventPublisher.addListener(new EventListener() {
      @Override
      public void onPreChange(PreChangeEvent event) {
        supportedCount.incrementAndGet();
      }

      @Override
      public void onChanged(ChangedEvent event) {
        // 不处理
      }

      @Override
      public boolean supports(String eventType) {
        return "PRE_UPDATE".equals(eventType); // 只支持UPDATE
      }

      @Override
      public int getOrder() {
        return 100;
      }
    });

    // 发布INSERT事件，不应该被处理
    PreInsertEvent insertEvent = new PreInsertEvent("test", "schema",
      Map.of("id", 1), 1, "session1", null);
    eventPublisher.publishPreChangeEvent(insertEvent);

    assertEquals(0, supportedCount.get());

    // 发布UPDATE事件，应该被处理
    PreUpdateEvent updateEvent = new PreUpdateEvent("test", "schema",
      Map.of("id", 1, "name", "old"), Map.of("id", 1, "name", "new"), 1, null, "session1", null);
    eventPublisher.publishPreChangeEvent(updateEvent);

    assertEquals(1, supportedCount.get());
  }

  // 辅助方法：创建测试数据源（需要根据实际情况实现）
  private DataSourceProvider createTestDataSource() {
    // 这里需要根据实际的测试环境创建数据源
    // 可以使用H2内存数据库进行测试
    return null; // 简化实现
  }
}
