package com.innotime.trainerapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.innotime.trainerapp.data.local.database.TrainerDatabase
import com.innotime.trainerapp.data.local.entity.AthleteEntity
import com.innotime.trainerapp.domain.model.TrainingGroup
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
            id = "group-1",
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
        val athlete1 = AthleteEntity(id = "athlete-1", name = "John")
        val athlete2 = AthleteEntity(id = "athlete-2", name = "Jane")
        database.athleteDao().insertAthlete(athlete1)
        database.athleteDao().insertAthlete(athlete2)

        // Add group with members
        val group = TrainingGroup(
            id = "group-1",
            name = "Elite Squad",
            memberIds = listOf("athlete-1", "athlete-2")
        )

        repository.addGroup(group)

        // Verify members are stored
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).containsExactly("athlete-1", "athlete-2")
        }
    }

    @Test
    fun updateGroup_updatesGroupName() = runTest {
        val group = TrainingGroup("group-1", "Original Name", emptyList())
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
        val group = TrainingGroup("group-1", "Elite Squad", emptyList())
        repository.addGroup(group)

        repository.deleteGroup("group-1")

        repository.getAllGroups().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun addMember_addsAthleteToGroup() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup("group-1", "Elite Squad", emptyList())
        repository.addGroup(group)

        // Add member
        repository.addMember("group-1", "athlete-1")

        // Verify
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).contains("athlete-1")
        }
    }

    @Test
    fun addMember_multipleTimes_maintainsSingleMembership() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup("group-1", "Elite Squad", emptyList())
        repository.addGroup(group)

        // Add member twice (should be idempotent)
        repository.addMember("group-1", "athlete-1")
        repository.addMember("group-1", "athlete-1")

        // Verify only one membership
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).hasSize(1)
        }
    }

    @Test
    fun removeMember_removesAthleteFromGroup() = runTest {
        // Setup
        val athlete1 = AthleteEntity(id = "athlete-1", name = "John")
        val athlete2 = AthleteEntity(id = "athlete-2", name = "Jane")
        database.athleteDao().insertAthlete(athlete1)
        database.athleteDao().insertAthlete(athlete2)

        val group = TrainingGroup(
            "group-1",
            "Elite Squad",
            listOf("athlete-1", "athlete-2")
        )
        repository.addGroup(group)

        // Remove one member
        repository.removeMember("group-1", "athlete-1")

        // Verify
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).containsExactly("athlete-2")
        }
    }

    @Test
    fun cascadeDelete_groupDeleteRemovesMemberships() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup("group-1", "Elite Squad", listOf("athlete-1"))
        repository.addGroup(group)

        // Delete group
        repository.deleteGroup("group-1")

        // Verify no orphaned memberships (implicitly tested by no errors)
        repository.getAllGroups().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun cascadeDelete_athleteDeleteRemovesMemberships() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        database.athleteDao().insertAthlete(athlete)

        val group = TrainingGroup("group-1", "Elite Squad", listOf("athlete-1"))
        repository.addGroup(group)

        // Delete athlete (should remove from group)
        database.athleteDao().deleteAthlete("athlete-1")

        // Verify membership removed
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).isEmpty()
        }
    }

    @Test
    fun getAllGroups_sortedByName() = runTest {
        // Add groups in non-alphabetical order
        repository.addGroup(TrainingGroup("group-1", "Zebra Squad", emptyList()))
        repository.addGroup(TrainingGroup("group-2", "Alpha Team", emptyList()))
        repository.addGroup(TrainingGroup("group-3", "Beta Group", emptyList()))

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
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        database.athleteDao().insertAthlete(athlete)

        // Add athlete to multiple groups
        val group1 = TrainingGroup("group-1", "Morning Team", listOf("athlete-1"))
        val group2 = TrainingGroup("group-2", "Elite Squad", listOf("athlete-1"))
        repository.addGroup(group1)
        repository.addGroup(group2)

        // Verify athlete is in both groups
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups).hasSize(2)
            assertThat(groups[0].memberIds).contains("athlete-1")
            assertThat(groups[1].memberIds).contains("athlete-1")
        }
    }

    @Test
    fun groupWithManyMembers_handlesCorrectly() = runTest {
        // Setup many athletes
        val athleteIds = (1..20).map { id ->
            val athlete = AthleteEntity(id = "athlete-$id", name = "Athlete $id")
            database.athleteDao().insertAthlete(athlete)
            "athlete-$id"
        }

        // Add group with many members
        val group = TrainingGroup("group-1", "Large Squad", athleteIds)
        repository.addGroup(group)

        // Verify all members stored
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertThat(groups[0].memberIds).hasSize(20)
            assertThat(groups[0].memberIds).containsExactlyElementsIn(athleteIds)
        }
    }
}
