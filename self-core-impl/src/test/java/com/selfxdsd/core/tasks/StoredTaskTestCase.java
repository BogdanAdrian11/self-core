/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permission is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.core.tasks;

import com.selfxdsd.api.*;
import com.selfxdsd.api.storage.Storage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unit tests for {@link StoredTask}.
 *
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class StoredTaskTestCase {

    /**
     * StoredTask can return its Project.
     */
    @Test
    public void returnsProject() {
        final Project project = Mockito.mock(Project.class);
        final Task task = new StoredTask(
            project,
            "issueId123",
            Contract.Roles.DEV,
            60,
            Mockito.mock(Storage.class)
        );
        MatcherAssert.assertThat(task.project(), Matchers.is(project));
    }

    /**
     * StoredTask can return its Issue.
     */
    @Test
    public void returnsIssue() {
        final Issue issue = Mockito.mock(Issue.class);
        final Issues all = Mockito.mock(Issues.class);
        Mockito.when(all.getById("123")).thenReturn(issue);
        final Repo repo = Mockito.mock(Repo.class);
        Mockito.when(repo.issues()).thenReturn(all);
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.repo()).thenReturn(repo);

        final Task task = new StoredTask(
            project,
            "123",
            Contract.Roles.DEV,
            60,
            Mockito.mock(Storage.class)
        );
        MatcherAssert.assertThat(task.issue(), Matchers.is(issue));
    }

    /**
     * StoredTask can return the role.
     */
    @Test
    public void returnsRole() {
        final Task task = new StoredTask(
            Mockito.mock(Project.class),
            "issueId123",
            Contract.Roles.DEV,
            60,
            Mockito.mock(Storage.class)
        );
        MatcherAssert.assertThat(
            task.role(),
            Matchers.equalTo(Contract.Roles.DEV)
        );
    }

    /**
     * Returns null when the Task is not assigned.
     */
    @Test
    public void returnsNullAssignee() {
        final Task task = new StoredTask(
            Mockito.mock(Project.class),
            "issueId123",
            Contract.Roles.DEV,
            60,
            Mockito.mock(Storage.class)
        );
        MatcherAssert.assertThat(
            task.assignee(), Matchers.nullValue()
        );
    }

    /**
     * StoredTask can return its assignment date.
     */
    @Test
    public void returnsAssignmentDate() {
        final LocalDateTime assignment = LocalDateTime.now();
        final Task task = new StoredTask(
            Mockito.mock(Contract.class),
            "issueId123",
            Mockito.mock(Storage.class),
            assignment,
            assignment.plusDays(10),
            0
        );
        MatcherAssert.assertThat(
            task.assignmentDate().isEqual(assignment),
            Matchers.is(Boolean.TRUE)
        );
    }

    /**
     * StoredTask can return its deadline date.
     */
    @Test
    public void returnsDeadline() {
        final LocalDateTime assignment = LocalDateTime.now();
        final Task task = new StoredTask(
            Mockito.mock(Contract.class),
            "issueId123",
            Mockito.mock(Storage.class),
            assignment,
            assignment.plusDays(10),
            0
        );
        MatcherAssert.assertThat(
            task.deadline().isEqual(assignment.plusDays(10)),
            Matchers.is(Boolean.TRUE)
        );
    }

    /**
     * Returns the assignee Contributor.
     */
    @Test
    public void returnsAssignee() {
        final Contributor mihai = Mockito.mock(Contributor.class);
        final Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.contributor()).thenReturn(mihai);

        final Task task = new StoredTask(
            contract,
            "issueId123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            0
        );
        MatcherAssert.assertThat(task.assignee(), Matchers.is(mihai));
    }

    /**
     * StoredTask can return its value based on the Contract's hourly rate and
     * its estimation.
     */
    @Test
    public void returnsValue() {
        final Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.hourlyRate()).thenReturn(
            BigDecimal.valueOf(50000)
        );

        final Task task = new StoredTask(
            contract,
            "issueId123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            30
        );

        MatcherAssert.assertThat(
            task.value(),
            Matchers.equalTo(BigDecimal.valueOf(25000))
        );
    }

    /**
     * StoredTask can return its value which is rounded half up.
     */
    @Test
    public void returnsValueRoundedUp() {
        final Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.hourlyRate()).thenReturn(
            BigDecimal.valueOf(25000)
        );

        final Task task = new StoredTask(
            contract,
            "issueId123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            45
        );

        MatcherAssert.assertThat(
            task.value(),
            Matchers.equalTo(BigDecimal.valueOf(18750))
        );
    }

    /**
     * StoredTask's value is zero if the Contract's hourly rate is zero.
     */
    @Test
    public void returnsZeroValue() {
        final Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.hourlyRate()).thenReturn(
            BigDecimal.valueOf(0)
        );

        final Task task = new StoredTask(
            contract,
            "issueId123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            120
        );

        MatcherAssert.assertThat(
            task.value(),
            Matchers.equalTo(BigDecimal.valueOf(0))
        );
    }

    /**
     * StoredTask returns its estimation when the task is assigned (has a
     * contract).
     */
    @Test
    public void returnsEstimationAssigned() {
        final Task task = new StoredTask(
            Mockito.mock(Contract.class),
            "issueId123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            120
        );

        MatcherAssert.assertThat(
            task.estimation(),
            Matchers.equalTo(120)
        );
    }

    /**
     * When the StoredTask is not assigned to anyone, the estimation should be
     * 0.
     */
    @Test
    public void returnsEstimation() {
        final Task task = new StoredTask(
            Mockito.mock(Project.class),
            "issueId123",
            Contract.Roles.DEV,
            45,
            Mockito.mock(Storage.class)
        );

        MatcherAssert.assertThat(
            task.estimation(),
            Matchers.equalTo(45)
        );
    }

    /**
     * Can compare two StoredTask objects.
     */
    @Test
    public void comparesStoredTaskObjects() {
        final Contract contract = Mockito.mock(Contract.class);
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.repoFullName()).thenReturn("john/repo");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        final Issue issue = Mockito.mock(Issue.class);
        Mockito.when(issue.issueId()).thenReturn("123");
        final Issues all = Mockito.mock(Issues.class);
        Mockito.when(all.getById("123")).thenReturn(issue);
        final Repo repo = Mockito.mock(Repo.class);
        Mockito.when(repo.issues()).thenReturn(all);
        Mockito.when(project.repo()).thenReturn(repo);
        Mockito.when(contract.project()).thenReturn(project);
        final Task task = new StoredTask(
            contract,
            "123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            120
        );
        final Task taskTwo = new StoredTask(
            contract,
            "123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            120
        );
        MatcherAssert.assertThat(task, Matchers.equalTo(taskTwo));
    }

    /**
     * Verifies HashCode generation from StoredTask.
     */
    @Test
    public void verifiesStoredTaskHashcode() {
        final Contract contract = Mockito.mock(Contract.class);
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.repoFullName()).thenReturn("john/repo");
        Mockito.when(project.provider()).thenReturn(Provider.Names.GITHUB);
        final Issue issue = Mockito.mock(Issue.class);
        final Issues all = Mockito.mock(Issues.class);
        Mockito.when(all.getById("123")).thenReturn(issue);
        final Repo repo = Mockito.mock(Repo.class);
        Mockito.when(repo.issues()).thenReturn(all);
        Mockito.when(project.repo()).thenReturn(repo);
        Mockito.when(contract.project()).thenReturn(project);
        final Task task = new StoredTask(
            contract,
            "123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            120
        );
        final Task taskTwo = new StoredTask(
            contract,
            "123",
            Mockito.mock(Storage.class),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10),
            120
        );
        MatcherAssert.assertThat(task.hashCode(),
            Matchers.equalTo(taskTwo.hashCode()));
    }
}
