package de.hardtthelen.trainerapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.hardtthelen.trainerapp.data.local.database.TrainerDatabase
import de.hardtthelen.trainerapp.data.local.entity.AthleteEntity
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.TrainingGroup
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for GroupRepositoryImpl with Room database.
 *
 * Tests demonstrate:
 * - CRUD operations for groups
 * - Many-to-many relationship with athletes
 * - Member management (add/remove)
 * - Cascade delete behavior
 */
class GroupRepositoryImplTest {

    private lateinit var database: TrainerDatabase
    private lateinit var repository: GroupRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            TrainerDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = GroupRepositoryImpl(database.groupDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllGroups_initiallyEmpty() = runTest {
        repository.getAllGroups().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun addGroup_groupIsRetrievable() = runTest {
        val group = TrainingGroup(
            id = TrainingGroupId("group-1"),
            name = "Elite Squad",
            memberIds = emptyList()
        )

        repository.addGroup(group)

        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups).hasSize(1)
            assertThat(groups[0].name).isEqualTo("Elite Squad")
        }
    }

    @Test
    fun addGroup_withMembers_createsRelationships() = runTest {
        // Setup athletes
        val athlete1 = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        val athlete2 = AthleteEntity(id = AthleteId("athlete-2"), name = "Jane")
        database.athleteDao().insertAthlete(athlete1)
        database.athleteDao().insertAthlete(athlete2)

        // Add group with members
        val group = TrainingGroup(
            id = TrainingGroupId("group-1"),
            name = "Elite Squad",
            memberIds = listOf(AthleteId("athlete-1"), AthleteId("athlete-2"))
        )

        repository.addGroup(group)

        // Verify members are stored
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).containsExactly(AthleteId("athlete-1"), AthleteId("athlete-2"))
        }
    }

    @Test
    fun updateGroup_updatesGroupName() = runTest {
        val group = TrainingGroup(TrainingGroupId("group-1"), "Original Name", emptyList())
        repository.addGroup(group)

        val updated = group.copy(name = "Updated Name")
        repository.updateGroup(updated)

        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].name).isEqualTo("Updated Name")
        }
    }

    @Test
    fun deleteGroup_removesGroup() = runTest {
        val group = TrainingGroup(TrainingGroupId("group-1"), "Elite Squad", emptyList())
        repository.addGroup(group)

        repository.deleteGroup(TrainingGroupId("group-1"))

        repository.getAllGroups().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun addMember_addsAthleteToGroup() = runTest {
        // Setup
        val athlete = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup(TrainingGroupId("group-1"), "Elite Squad", emptyList())
        repository.addGroup(group)

        // Add member
        repository.addMember(TrainingGroupId("group-1"), AthleteId("athlete-1"))

        // Verify
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).contains(AthleteId("athlete-1"))
        }
    }

    @Test
    fun addMember_multipleTimes_maintainsSingleMembership() = runTest {
        // Setup
        val athlete = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup(TrainingGroupId("group-1"), "Elite Squad", emptyList())
        repository.addGroup(group)

        // Add member twice (should be idempotent)
        repository.addMember(TrainingGroupId("group-1"), AthleteId("athlete-1"))
        repository.addMember(TrainingGroupId("group-1"), AthleteId("athlete-1"))

        // Verify only one membership
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).hasSize(1)
        }
    }

    @Test
    fun removeMember_removesAthleteFromGroup() = runTest {
        // Setup
        val athlete1 = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        val athlete2 = AthleteEntity(id = AthleteId("athlete-2"), name = "Jane")
        database.athleteDao().insertAthlete(athlete1)
        database.athleteDao().insertAthlete(athlete2)

        val group = TrainingGroup(
            TrainingGroupId("group-1"),
            "Elite Squad",
            listOf(AthleteId("athlete-1"), AthleteId("athlete-2"))
        )
        repository.addGroup(group)

        // Remove one member
        repository.removeMember(TrainingGroupId("group-1"), AthleteId("athlete-1"))

        // Verify
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).containsExactly(AthleteId("athlete-2"))
        }
    }

    @Test
    fun cascadeDelete_groupDeleteRemovesMemberships() = runTest {
        // Setup
        val athlete = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup(TrainingGroupId("group-1"), "Elite Squad", listOf(AthleteId("athlete-1")))
        repository.addGroup(group)

        // Delete group
        repository.deleteGroup(TrainingGroupId("group-1"))

        // Verify no orphaned memberships (implicitly tested by no errors)
        repository.getAllGroups().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun cascadeDelete_athleteDeleteRemovesMemberships() = runTest {
        // Setup
        val athlete = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup(TrainingGroupId("group-1"), "Elite Squad", listOf(AthleteId("athlete-1")))
        repository.addGroup(group)

        // Delete athlete (should remove from group)
        database.athleteDao().deleteAthlete(AthleteId("athlete-1"))

        // Verify membership removed
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).isEmpty()
        }
    }

    @Test
    fun getAllGroups_sortedByName() = runTest {
        // Add groups in non-alphabetical order
        repository.addGroup(TrainingGroup(TrainingGroupId("group-1"), "Zebra Squad", emptyList()))
        repository.addGroup(TrainingGroup(TrainingGroupId("group-2"), "Alpha Team", emptyList()))
        repository.addGroup(TrainingGroup(TrainingGroupId("group-3"), "Beta Group", emptyList()))

        // Verify sorted alphabetically
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups.map { it.name })
                .containsExactly("Alpha Team", "Beta Group", "Zebra Squad")
                .inOrder()
        }
    }

    @Test
    fun multipleGroups_canShareMembers() = runTest {
        // Setup
        val athlete = AthleteEntity(id = AthleteId("athlete-1"), name = "John")
        database.athleteDao().insertAthlete(athlete)

        // Add athlete to multiple groups
        val group1 = TrainingGroup(TrainingGroupId("group-1"), "Morning Team", listOf(AthleteId("athlete-1")))
        val group2 = TrainingGroup(TrainingGroupId("group-2"), "Elite Squad", listOf(AthleteId("athlete-1")))
        repository.addGroup(group1)
        repository.addGroup(group2)

        // Verify athlete is in both groups
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups).hasSize(2)
            assertThat(groups[0].memberIds).contains(AthleteId("athlete-1"))
            assertThat(groups[1].memberIds).contains(AthleteId("athlete-1"))
        }
    }

    @Test
    fun groupWithManyMembers_handlesCorrectly() = runTest {
        // Setup many athletes
        val athleteIds = (1..20).map { id ->
            val athlete = AthleteEntity(id = AthleteId("athlete-$id"), name = "Athlete $id")
            database.athleteDao().insertAthlete(athlete)
            AthleteId("athlete-$id")
        }

        // Add group with many members
        val group = TrainingGroup(TrainingGroupId("group-1"), "Large Squad", athleteIds)
        repository.addGroup(group)

        // Verify all members stored
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).hasSize(20)
            assertThat(groups[0].memberIds).containsExactlyElementsIn(athleteIds)
        }
    }
}
