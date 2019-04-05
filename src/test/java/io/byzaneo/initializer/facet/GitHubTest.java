package io.byzaneo.initializer.facet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitHubTest {

    @Test
    public void testSlugOrganization() {
        assertEquals("my-org/my-repo", new GitHub("my-org", "my-repo").getSlug());
    }

    @Test
    public void testSlugUsername() {
        assertEquals("me/my-repo", new GitHub("me", null, "my-repo").getSlug());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlugNone() {
        new GitHub().getSlug();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlugOrganizationAndNoName() {
        new GitHub("my-org", null).getSlug();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlugUsernameAndNoName() {
        new GitHub("me", null, null).getSlug();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlugNoUsername() {
        new GitHub(null, null, "any").getSlug();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlugNoOrganization() {
        new GitHub(null, "any").getSlug();
    }

    @Test
    public void testToMap() {
        System.out.println(new GitHub("me", "secret", "repo").toProperties());
    }
}
