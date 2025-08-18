package com.example.task.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class mgmtaudits {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "mgmt_id")
        private Mgmt mgmt; // Management user performing the action

        private String action; // Action: created, updated, deleted, etc.

        private Long feedbackId; // ID of the feedback being acted upon

        private LocalDateTime timestamp; // Timestamp of the action

        // Constructors, getters, and setters
        public mgmtaudits() {}

        public mgmtaudits(Mgmt mgmt, String action, Long feedbackId, LocalDateTime timestamp) {
            this.mgmt = mgmt;
            this.action = action;
            this.feedbackId = feedbackId;
            this.timestamp = timestamp;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Mgmt getMgmt() {
            return mgmt;
        }

        public void setMgmt(Mgmt mgmt) {
            this.mgmt = mgmt;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Long getFeedbackId() {
            return feedbackId;
        }

        public void setFeedbackId(Long feedbackId) {
            this.feedbackId = feedbackId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }


