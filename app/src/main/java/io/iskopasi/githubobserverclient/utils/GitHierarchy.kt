package io.iskopasi.githubobserverclient.utils

import io.iskopasi.githubobserverclient.pojo.RepositoryContentData

class GitHierarchy(val data: List<RepositoryContentData>) {
    lateinit var flatStructure: List<RepositoryContentData>
    private lateinit var fastAccessMap: Map<String, RepositoryContentData>

    init {
        remap()
    }

    fun find(data: RepositoryContentData): RepositoryContentData? {
        return if (fastAccessMap.containsKey(data.sha)) fastAccessMap[data.sha] else null
    }

    // Updates content and refreshes inner structures
    fun addNestedContent(
        node: RepositoryContentData,
        nestedData: List<RepositoryContentData>
    ): List<RepositoryContentData> {
        val innerNode = find(node)

        if (innerNode == null) return listOf()

        val innerContent = innerNode.content

        if (innerContent.isEmpty()) {
            innerContent.addAll(nestedData)
        } else {
            innerContent.clear()
        }

        return remap()
    }

    private fun remap(): List<RepositoryContentData> {
        flatStructure = flatStructured()
        fastAccessMap = flatStructure.associate { Pair(it.sha, it) }

        return flatStructure
    }

    private fun flatStructured(): List<RepositoryContentData> {
        val newList = mutableListOf<RepositoryContentData>()

        data.forEach {
            newList.add(it)
            newList.addAll(it.flatStructured(it.level + 1))
        }

        return newList
    }

    fun hasContent(node: RepositoryContentData): Boolean {
        val innerNode = find(node)

        if (innerNode == null) return false

        return innerNode.content.isNotEmpty()
    }

    fun removeContentForNode(node: RepositoryContentData): List<RepositoryContentData> {
        val innerNode = find(node)

        if (innerNode == null) return listOf()

        innerNode.content.clear()
        return remap()
    }
}