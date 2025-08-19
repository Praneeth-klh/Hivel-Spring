package com.example.task.Service;

import com.example.task.model.Mgmt;
import com.example.task.repository.Mgmtrepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MgmtSer {

    @Autowired
    private Mgmtrepo m;
    private static final Logger logger = LoggerFactory.getLogger(MgmtSer.class);

    public Mgmt findByUsernameAndPassword(String username, String password) {
        Optional<Mgmt> mgmtOptional = m.findByUsername(username);

        if (mgmtOptional.isPresent()) {
            Mgmt mgmt = mgmtOptional.get();

            if (mgmt.getPassword().equals(password)) {
                return new Mgmt();
            }
        }
        return null;
    }

}
