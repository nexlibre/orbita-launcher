package org.nexlibre.orbita.core

import org.junit.Assert.*
import org.junit.Test

class LayoutTest {
    private fun key(i: Int) = "org.example$i/org.example$i.Main"
    @Test fun migrationKeepsOrderAndUnavailableReferences() {
        val data = LayoutCodec.migrate(listOf(key(1), key(2), key(3)), listOf(key(4)))
        assertEquals(listOf(0,1,2), data.items.filterNot { it.dock }.map { it.slot })
        assertEquals(listOf(key(1),key(2),key(3),key(4)), data.items.flatMap { it.members })
        assertEquals(data, LayoutCodec.decode(LayoutCodec.encode(data)))
        assertEquals(data, LayoutCodec.migrate(listOf(key(1), key(2), key(3)), listOf(key(4))))
    }
    @Test fun createsPagesWithoutReflowingSavedPositions() {
        var data = Layout()
        repeat(26) { data = data.add(key(it)) }
        assertEquals(2, data.pages)
        assertEquals(0, data.items[25].slot)
        assertEquals(1, data.items[25].page)
        assertEquals(24, data.items[24].slot)
    }
    @Test fun moveSwapsAcrossPagesAndDock() {
        var data = Layout().add(key(1)).add(key(2),dock=true).addPage()
        val a=data.items.first { !it.dock };val b=data.items.first { it.dock }
        data=data.move(a.id,0,0,true)
        assertTrue(data.items.first { it.id==a.id }.dock)
        assertFalse(data.items.first { it.id==b.id }.dock)
        data=data.move(a.id,1,7)
        assertEquals(7,data.items.first { it.id==a.id }.slot)
    }
    @Test fun foldersMoveMembersWithoutDuplicatesAndPreserveManualOrder() {
        var data=Layout().add(key(1)).add(key(2)).add(key(3))
        val id=data.items.first().id
        data=data.folder(id,"Carpeta").addToFolder(id,key(2)).addToFolder(id,key(3))
        assertEquals(1,data.items.size)
        data=data.reorder(id,key(3),-1)
        assertEquals(listOf(key(1),key(3),key(2)),data.items.single().members)
        data=data.add(key(3),dock=true)
        assertEquals(listOf(key(1),key(2)),data.items.first { it.folder }.members)
        assertEquals(3,data.items.flatMap { it.members }.distinct().size)
        data=data.folder(id,"Renombrada")
        assertEquals("Renombrada",LayoutCodec.decode(LayoutCodec.encode(data)).items.first { it.folder }.title)
    }
    @Test fun removingLastMemberRemovesEmptyFolder() {
        var data=Layout().add(key(1));val id=data.items.single().id
        data=data.folder(id,"Nombre").without(key(1))
        assertTrue(data.items.isEmpty())
    }
    @Test fun invalidAndUnknownDataAreRejected() {
        listOf("invalid", "AAAAAwAAAAEAAAAA", LayoutCodec.encode(Layout())+"AAAA").forEach {
            assertTrue(runCatching { LayoutCodec.decode(it) }.isFailure)
        }
        val item=Placement(members=listOf(key(1)))
        assertTrue(runCatching { Layout(items=listOf(item,item.copy(id="other"))).checked() }.isFailure)
        assertTrue(runCatching { Layout().add(key(1)).folder("absent","") }.isSuccess) // no mutation of unknown identity
    }
    @Test fun fullDockFailureLeavesInputUnchangedAndPageRemovalChecksOccupancy() {
        var data=Layout(); repeat(5) { data=data.add(key(it),dock=true) }
        val original=data
        assertTrue(runCatching { data.add(key(9),dock=true) }.isFailure)
        assertEquals(original,data)
        data=data.addPage().add(key(8),1)
        assertTrue(runCatching { data.removeLastPage() }.isFailure)
    }
    @Test fun repeatedMovesRoundTripWithoutLosingApps() {
        var data=Layout().addPage()
        repeat(40) { data=data.add(key(it)) }
        repeat(200) { n -> val item=data.items[n%40];data=data.move(item.id,n%2,n%25);data=LayoutCodec.decode(LayoutCodec.encode(data)) }
        assertEquals(40,data.items.flatMap { it.members }.distinct().size)
    }
}
