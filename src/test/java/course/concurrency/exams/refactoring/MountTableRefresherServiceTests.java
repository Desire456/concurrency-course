package course.concurrency.exams.refactoring;

import course.concurrency.exams.refactoring.Others.MountTableManager;
import course.concurrency.exams.refactoring.Others.MountTableManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsIn.in;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class MountTableRefresherServiceTests {

    private MountTableRefresherService service;
    private MountTableManagerFactory mountTableManagerFactory;
    private MountTableManager manager;

    private Others.RouterStore routerStore;
    private Others.LoadingCache routerClientsCache;

    @BeforeEach
    public void setUpStreams() {
        service = new MountTableRefresherService();
        service.setCacheUpdateTimeout(1000);
        mountTableManagerFactory = mock(MountTableManagerFactory.class);
        manager = mock(MountTableManager.class);
        service.setMountTableManagerFactory(mountTableManagerFactory);
        routerStore = mock(Others.RouterStore.class);
        service.setRouterStore(routerStore);
        routerClientsCache = mock(Others.LoadingCache.class);
        service.setRouterClientsCache(routerClientsCache);
        // service.serviceInit(); // needed for complex class testing, not for now
    }

    @AfterEach
    public void restoreStreams() {
        // service.serviceStop();
    }

    @Test
    @DisplayName("All tasks are completed successfully")
    public void allDone() {
        // given
        var mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true);
        when(mountTableManagerFactory.create(Mockito.any())).thenReturn(manager);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=4,failureCount=0");
        verify(routerClientsCache, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("All tasks failed")
    public void noSuccessfulTasks() {
        var mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(false);
        when(mountTableManagerFactory.create(Mockito.any())).thenReturn(manager);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=4");
        states.forEach(e -> verify(routerClientsCache).invalidate(e.getAdminAddress()));
    }

    @Test
    @DisplayName("Some tasks failed")
    public void halfSuccessedTasks() {
        var mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true, false, false, true);
        when(mountTableManagerFactory.create(Mockito.any())).thenReturn(manager);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // when
        mockedService.refresh();

        // then
        var modifiedAddresses = states.stream().map(Others.RouterState::getAdminAddress).collect(toList());
        verify(mockedService).log("Mount table entries cache refresh successCount=2,failureCount=2");
        verify(routerClientsCache, times(2)).invalidate(argThat(in(modifiedAddresses)));
    }

    @Test
    @DisplayName("One task completed with exception")
    public void exceptionInOneTask() {
        var mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        when(manager.refresh())
                .thenReturn(true, true, true)
                .thenThrow(new RuntimeException());
        when(mountTableManagerFactory.create(Mockito.any())).thenReturn(manager);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // when
        mockedService.refresh();

        // then
        var modifiedAddresses = states.stream().map(Others.RouterState::getAdminAddress).collect(toList());
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(routerClientsCache).invalidate(argThat(in(modifiedAddresses)));
    }

    @Test
    @DisplayName("One task with interrupted exception")
    public void interruptedExceptionInOneTask() {
        var mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        when(manager.refresh())
                .thenReturn(true, true, true)
                .thenAnswer(inv -> {
                    throw new InterruptedException();
                });
        when(mountTableManagerFactory.create(Mockito.any())).thenReturn(manager);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // when
        mockedService.refresh();

        // then
        var modifiedAddresses = states.stream().map(Others.RouterState::getAdminAddress).collect(toList());
        verify(mockedService).log("Mount table cache refresher was interrupted.");
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(routerClientsCache).invalidate(argThat(in(modifiedAddresses)));
    }

    @Test
    @DisplayName("One task exceeds timeout")
    public void oneTaskExceedTimeout() {
        var mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        when(manager.refresh())
                .thenReturn(true, true, true)
                .thenAnswer(inv -> {
                    Thread.sleep(3000);
                    return true;
                });
        when(mountTableManagerFactory.create(Mockito.any())).thenReturn(manager);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        // when
        mockedService.refresh();

        // then
        var modifiedAddresses = states.stream().map(Others.RouterState::getAdminAddress).collect(toList());
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(mockedService).log("Not all router admins updated their cache");
        verify(routerClientsCache).invalidate(argThat(in(modifiedAddresses)));
    }

}
