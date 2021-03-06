package com.twelvebooks.twelvebook.domain;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "notices")
@Getter
@Setter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    @Lob
    private String content;

    @Type(type = "date")
    private Date regdate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "notice",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<NoticeImageFile> noticeImageFiles;

    public Notice() {
        noticeImageFiles = new ArrayList<>();
        regdate = new Date();
    }

    public void addImageFile(NoticeImageFile noticeImageFile) {
        if(noticeImageFiles == null){
            noticeImageFiles = new ArrayList<>();
        }
        noticeImageFile.setNotice(this);
        noticeImageFiles.add(noticeImageFile);
    }
}
