package com.fabcode.storefragment.dao;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fabcode.storefragment.entity.Account;



//import entity.Account;

@Transactional
@Repository
public class AccountDAO {
	@Autowired
    private SessionFactory sessionFactory;

    public Account findAccount(String userName) {
        Session session = this.sessionFactory.getCurrentSession();
        return  session.find(Account.class, userName);
    }

	public Account save(Account user) {
		// TODO Auto-generated method stub
		((Session) sessionFactory).save(user);
		return user;
	}

	public Account findByUserName(String userName) {
		// TODO Auto-generated method stub
		return null;
	}


	

}
