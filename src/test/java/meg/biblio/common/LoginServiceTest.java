package meg.biblio.common;

import junit.framework.Assert;
import meg.biblio.common.db.RoleRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.RoleDao;
import meg.biblio.common.db.dao.UserLoginDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

	@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
	@RunWith(SpringJUnit4ClassRunner.class)
	@Transactional
	public class LoginServiceTest {

		@Autowired
		private LoginService service;

		@Autowired
	    RoleRepository roleRepository;	
		
		private UserLoginDao account;
		private RoleDao role;

		@Before
		public void initData() {
			account = new UserLoginDao();
			account.setUsername("predefined@email.com");
			account.setTextpassword("password");
			
			account = service.createNewUserLogin(account, 1L);
			
			role = new RoleDao();
			role.setRolename("ROLE_PEANUT");
			role = roleRepository.save(role);
			
		}

		@SuppressWarnings("deprecation")
		@Test
		@Transactional
		public void testCreateUserLoginDao() {
			// create valid account
			UserLoginDao acct = new UserLoginDao();
			acct.setUsername("test.email@email.com");
			acct.setTextpassword("password");
			
			// save it using the service
			acct = service.createNewUserLogin(acct,1L);

			// check that it is not-null, and has an id
			Assert.assertNotNull(acct);
			Assert.assertNotNull(acct.getId());
			// check that the password is present
			Assert.assertNotNull(acct.getPassword());
			Assert.assertTrue(!acct.getPassword().equals("password"));
			
			// Test Password encoding
			StandardPasswordEncoder encoder = new StandardPasswordEncoder();
			boolean match = encoder.matches("password", acct.getPassword());
			Assert.assertTrue(match);
		}


		
		@Test 
		@Transactional
		public void testUserExists() {
			
			// call to accountService.userExists with same email as
			// in initData should return true
			boolean returncall = service.userExists("predefined@email.com");
			Assert.assertTrue(returncall);
		}
		
		@Test
		@Transactional
		public void testUpdateUser() {
			// create account and save in the db
			UserLoginDao acct = new UserLoginDao();
			acct.setTextpassword("password");
			acct.setUsername("usernametestpasschange");
			acct.setPasswordverify("password");
			acct = service.createNewUserLogin(acct, 1L);
			Long id = acct.getId();
			
			// pull account
			acct = service.getUserLoginDaoById(id);
			// set old password, text and verify
			acct.setOldpassword("password");
			acct.setTextpassword("newpass");
			acct.setPasswordverify("newpass");
			
			// call service updateUserLoginDao
			service.updateUserLoginDao(acct);
			
			// verify that password now matches text, and not oldpassword
			StandardPasswordEncoder encoder = new StandardPasswordEncoder();
			boolean match = encoder.matches("newpass", acct.getPassword());
			Assert.assertTrue(match);		
		}
		
		@Test
		@Transactional
		public void testOldPasswordMatches() {
			Long testid = account.getId();
			// call service method with initData account, and "password"
			boolean match = service.oldPasswordMatches("password", testid);
			// Assert return value is true
			Assert.assertTrue(match);
			// call service method with initData account, and "george"
			match = service.oldPasswordMatches("george", testid);
			// Assert return value is false
			Assert.assertFalse(match);
		}
		
		@Test
		public void testGetClientForUsername() {
			// service call for ("predefined@email.com") (defined during init)
			ClientDao client = service.getClientForUsername("predefined@email.com");
			
			// assert not null, and id = 1
			Assert.assertNotNull(client);
			Assert.assertEquals(new Long(1),client.getId());
		}
	}