package dev.tapngo.app.utils.inventreeutils.components

class Job(
    val id: Int,
    val address: Address,
    val status: Int,
    val name: String,
    val description: String,
    val items: List<JobItem>
)