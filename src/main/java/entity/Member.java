package entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;
    private int age;
    @ManyToOne
    @JoinColumn(name = "ID")
    private Team team;
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<Order>();
}
