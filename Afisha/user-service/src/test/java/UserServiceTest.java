import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.it.lab.UserProto;
import ru.it.lab.dao.AbstractDao;
import ru.it.lab.dao.impl.RoleDaoImpl;
import ru.it.lab.dao.impl.UserDaoImpl;
import ru.it.lab.entities.AbstractEntity;
import ru.it.lab.entities.Role;
import ru.it.lab.entities.User;
import ru.it.lab.enums.GenderType;
import ru.it.lab.enums.RoleType;
import ru.it.lab.service.UserServerService;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Random.class)
public class UserServiceTest {
    public Map<Long, User> userMap = new HashMap<>();
    private Long userId = 1L;
    public Map<Long, Role> roleMap = new HashMap<>();


    @Mock
    private UserDaoImpl userDao;

    @Mock
    private RoleDaoImpl roleDao;

    @InjectMocks
    private UserServerService userService;

    @BeforeEach
    public void setup() {
        setupRoleDao();
        setupUserDao();
        fillRoleTable();
        fillUserTable();
    }

    @Test
    void getLoginData_whenUserDontExist_thenThrowsEntityNotFoundException() throws Exception {
        UserProto request = UserProto.newBuilder()
                .setUsername("test")
                .build();
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
        assertThrows(EntityNotFoundException.class, () -> userService.getLoginData(request, responseObserver));
    }

    @Test
    void getLoginData_whenUserExists_thenSuccess() throws Exception {
        String searchedUsername = "desertfox";
        UserProto request = UserProto.newBuilder()
                .setUsername(searchedUsername)
                .build();
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
       userService.getLoginData(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)){
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        List<UserProto> responseList = responseObserver.getValues();
        assertEquals(1, responseList.size());
        UserProto response = responseList.get(0);
        assertEquals(searchedUsername,response.getUsername());
    }




    private <T extends AbstractEntity> void setupGetById(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (hashmap.containsKey(id)) {
                return hashmap.get(id);
            }
            String[] splittedClassName = className.getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }).when(dao).getById(anyLong());
    }

    private <T extends AbstractEntity> void setupDeleteById(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (hashmap.containsKey(id)) {
                hashmap.remove(id);
                return null;
            }
            String[] splittedClassName = className.getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }).when(dao).deleteById(anyLong());
    }

    private <T extends AbstractEntity> void setupCreate(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            T entity = invocationOnMock.getArgument(0);
            Long id = (long) hashmap.size();
            while (hashmap.containsKey(id)) {
                id++;
            }
            entity.setId(id);
            hashmap.put(id, entity);
            return entity;
        }).when(dao).create(any(className));
    }


    private void setupRoleDao() {
        setupGetById(roleMap, roleDao, Role.class);
        lenient().doAnswer(invocationOnMock -> {
            Role role = invocationOnMock.getArgument(0);
            Long id = (long) roleMap.size();
            while (roleMap.containsKey(id)) {
                id++;
            }
            role.setId(id);
            role.setPermissions(new ArrayList<>());
            roleMap.put(id, role);
            return role;
        }).when(roleDao).create(any(Role.class));
        setupDeleteById(roleMap, roleDao, Role.class);
    }

    private void setupUserDao() {
        setupGetById(userMap, userDao, User.class);
        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            user.setId(userId);
            userMap.put(userId, user);
            userId++;
            return user;
        }).when(userDao).create(any(User.class));

        setupDeleteById(userMap, userDao, User.class);

        lenient().doAnswer(invocationOnMock -> {
            String username = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByUsername(any(String.class));

        lenient().doAnswer(invocationOnMock -> {
            String restoreCode = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (restoreCode.equals(user.getRestorePasswordCode())) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByResetCode(any(String.class));

        lenient().doAnswer(invocationOnMock -> {
            String activationCode = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (activationCode.equals(user.getActivationCode())) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByActivationCode(any(String.class));
    }

    private void addUser(Long id, String username, String password, String email, Date dateOfBirth, GenderType genderType, Long roleId, Boolean isOpenProfile) {
        Role role = roleMap.get(roleId);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setDateOfBirth(dateOfBirth);
        user.setGenderType(genderType);
        user.setRole(role);
        user.setIsBanned(false);
        user.setIsOpenProfile(isOpenProfile);
        user.setActivationCode(null);
        user.setRestorePasswordCode(null);
        this.userMap.put(id, user);
        userId++;
    }

    private void fillUserTable() {
        addUser(1L, "desertfox", "$2a$10$dCKE0qv1SW3dKBTXkauFburkrCGOznBAhdXaV3Km9yre7qysphk1u", "fess.2002@mail.ru", new Date(9, Calendar.NOVEMBER, 2001), GenderType.MALE, 1L, true);
        addUser(2L, "aleoonka", "$2a$10$Uimw7bv5iTa.5miRSn4M4uGosxfyh1d89aVEHIkSNsPFkz6NmgOLq", "fess.2002@mail.ru", new Date(23, Calendar.JULY, 2002), GenderType.FEMALE, 2L, true);
    }

    private void fillRoleTable() {
        Role admin = new Role();
        admin.setName(RoleType.ADMIN);
        admin.setPermissions(new ArrayList<>());
        Role user = new Role();
        user.setName(RoleType.USER);
        user.setPermissions(new ArrayList<>());
        Role organizer = new Role();
        organizer.setName(RoleType.ORGANIZER);
        organizer.setPermissions(new ArrayList<>());
        roleMap.put(1L, admin);
        roleMap.put(2L, user);
        roleMap.put(3L, organizer);
    }
}
