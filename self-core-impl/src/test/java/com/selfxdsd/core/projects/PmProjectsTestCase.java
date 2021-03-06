/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.core.projects;

import com.selfxdsd.api.*;
import com.selfxdsd.api.storage.Paged;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Unit tests for {@link PmProjects}.
 *
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class PmProjectsTestCase {

    /**
     * Registering a new Repo within a PM's Projects is
     * not allowed.
     */
    @Test(expected = IllegalStateException.class)
    public void registerIsForbidden() {
        final Projects projects = new PmProjects(1, Stream::empty);
        projects.register(
            Mockito.mock(Repo.class),
            Mockito.mock(ProjectManager.class),
            "wh123token"
        );
    }

    /**
     * Method assignedTo() returns itself if the id matches.
     */
    @Test
    public void assignedToReturnsItself() {
        final Projects projects = new PmProjects(1, Stream::empty);
        MatcherAssert.assertThat(
            projects.assignedTo(1), Matchers.is(projects)
        );
    }

    /**
     * Method assignedTo() throws an exception if the specified id
     * is the one of a different PM.
     */
    @Test(expected = IllegalStateException.class)
    public void assignedToComplainsOnDifferendId() {
        final Projects projects = new PmProjects(1, Stream::empty);
        projects.assignedTo(2);
    }

    /**
     * PmProjects can be iterated.
     */
    @Test
    public void iterateWorks() {
        final List<Project> list = new ArrayList<>();
        list.add(Mockito.mock(Project.class));
        list.add(Mockito.mock(Project.class));
        list.add(Mockito.mock(Project.class));
        final Projects projects = new PmProjects(1, list::stream);
        MatcherAssert.assertThat(projects, Matchers.iterableWithSize(3));
    }

    /**
     * Method ownedBy(User) returns the User's projects.
     */
    @Test
    public void ownedByWorks() {
        final List<Project> list = new ArrayList<>();
        list.add(this.projectOwnedBy("mihai", "github"));
        list.add(this.projectOwnedBy("mihai", "github"));
        list.add(this.projectOwnedBy("vlad", "github"));
        list.add(this.projectOwnedBy("mihai", "gitlab"));
        list.add(this.projectOwnedBy("mihai", "github"));
        final Projects projects = new PmProjects(1, list::stream);
        MatcherAssert.assertThat(
            projects.ownedBy(
                this.mockUser("mihai", "github")
            ),
            Matchers.iterableWithSize(3)
        );
    }

    /**
     * Mock a Project owned by a User.
     *
     * @param username User's name.
     * @param providerName Provider's name.
     * @return Project.
     */
    private Project projectOwnedBy(
        final String username, final String providerName
    ) {
        final Project project = Mockito.mock(Project.class);
        final User owner = this.mockUser(username, providerName);
        Mockito.when(project.owner()).thenReturn(owner);

        return project;
    }

    /**
     * Should find a project by it's id.
     */
    @Test
    public void projectByIdFound() {
        final Projects projects = new PmProjects(
            1,
            () -> List.of(
                mockProject("john/test", "github"),
                mockProject("john/test2", "github")
            ).stream()
        );
        final Project found = projects.getProjectById("john/test", "github");
        MatcherAssert.assertThat(
            found.repoFullName(),
            Matchers.equalTo("john/test")
        );
        MatcherAssert.assertThat(
            found.provider(),
            Matchers.equalTo("github")
        );
    }

    /**
     * Should return null is project is not found by id.
     */
    @Test
    public void projectByIdNotFound() {
        final Projects projects = new PmProjects(1, Stream::empty);
        MatcherAssert.assertThat(
            projects.getProjectById("mihai/missing", "github"),
            Matchers.nullValue()
        );
    }

    /**
     * Should a iterate over an existing page.
     */
    @Test
    public void iteratePageWorks(){
        final Projects projects = new PmProjects(1, () -> IntStream
            .rangeClosed(1, 14)
            .mapToObj(i -> mockProject("repo-" + i,
                Provider.Names.GITHUB)));
        MatcherAssert.assertThat(projects, Matchers.iterableWithSize(10));
        MatcherAssert.assertThat(projects.page(new Paged.Page(2, 5)),
            Matchers.iterableWithSize(5));
        MatcherAssert.assertThat(projects.page(new Paged.Page(3, 5)),
            Matchers.iterableWithSize(4));
        MatcherAssert.assertThat(projects.page(new Paged.Page(1, 15)),
            Matchers.iterableWithSize(14));
        MatcherAssert.assertThat(projects.page(new Paged.Page(14, 1)),
            Matchers.iterableWithSize(1));
    }

    /**
     * Should a iterate over an empty page.
     */
    @Test
    public void iterateEmptyPageWorks(){
        final Projects projects = new PmProjects(1, Stream::empty);
        MatcherAssert.assertThat(projects, Matchers.emptyIterable());
    }

    /**
     * Throws when page is upper out of bounds.
     */
    @Test(expected = IllegalStateException.class)
    public void throwsWhenPageIsUpperOutOfBound(){
        new PmProjects(1, () -> IntStream
            .rangeClosed(1, 10)
            .mapToObj(i -> mockProject("repo-" + i,
                Provider.Names.GITHUB)))
            .page(new Paged.Page(5, 10));
    }

    /**
     * Throws when page is under out of bounds.
     */
    @Test(expected = IllegalStateException.class)
    public void throwsWhenPageIsUnderOutOfBound(){
        new PmProjects(1, () -> IntStream
            .rangeClosed(1, 10)
            .mapToObj(i -> mockProject("repo-" + i,
                Provider.Names.GITHUB)))
            .page(new Paged.Page(0, 10));
    }

    /**
     * Should find a project by it's id in a random page.
     */
    @Test
    public void projectByIdFoundInPage(){
        final Project found = new PmProjects(1, () -> IntStream
            .rangeClosed(1, 14)
            .mapToObj(i -> mockProject("repo-" + i,
                Provider.Names.GITHUB)))
            .page(new Paged.Page(2, 5))
            .getProjectById("repo-7", Provider.Names.GITHUB);
        MatcherAssert.assertThat(
            found.repoFullName(),
            Matchers.equalTo("repo-7")
        );
        MatcherAssert.assertThat(
            found.provider(),
            Matchers.equalTo("github")
        );
    }

    /**
     * Should return null if project is not found by id in the specified page,
     * even though project exists in overall.
     */
    @Test
    public void existingProjectNotFoundByIdInPage(){
        final Project found = new PmProjects(1, () -> IntStream
            .rangeClosed(1, 14)
            .mapToObj(i -> mockProject("repo-" + i,
                Provider.Names.GITHUB)))
            .page(new Paged.Page(2, 5))
            .getProjectById("repo-1", Provider.Names.GITHUB);
        MatcherAssert.assertThat(found, Matchers.nullValue());
    }


    /**
     * Method ownedBy(User) returns the User's projects at the specified page.
     */
    @Test
    public void ownedByInPageWorks() {
        final List<Project> list = new ArrayList<>();
        list.add(this.projectOwnedBy("mihai", "github"));
        list.add(this.projectOwnedBy("mihai", "github"));
        list.add(this.projectOwnedBy("vlad", "github"));
        list.add(this.projectOwnedBy("mihai", "gitlab"));
        list.add(this.projectOwnedBy("mihai", "github"));
        final Projects projects = new PmProjects(1, list::stream);
        MatcherAssert.assertThat(
            projects.page(new Paged.Page(1, 3))
                .ownedBy(this.mockUser("mihai", "github")),
            Matchers.iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            projects.page(new Paged.Page(2, 3))
                .ownedBy(this.mockUser("mihai", "github")),
            Matchers.iterableWithSize(1)
        );
    }


    /**
     * Mock a User.
     *
     * @param username Username.
     * @param providerName Name of the provider.
     * @return User.
     */
    private User mockUser(final String username, final String providerName) {
        final User user = Mockito.mock(User.class);
        Mockito.when(user.username()).thenReturn(username);

        final Provider provider = Mockito.mock(Provider.class);
        Mockito.when(provider.name()).thenReturn(providerName);

        Mockito.when(user.provider()).thenReturn(provider);
        return user;
    }

    /**
     * Mocks a bare minimum project.
     *
     * @param repoFullName Repo full name.
     * @param repoProvider Provider.
     * @return Mocked Project
     */
    private Project mockProject(
        final String repoFullName,
        final String repoProvider
    ) {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.repoFullName()).thenReturn(repoFullName);
        Mockito.when(project.provider()).thenReturn(repoProvider);
        return project;
    }
}
