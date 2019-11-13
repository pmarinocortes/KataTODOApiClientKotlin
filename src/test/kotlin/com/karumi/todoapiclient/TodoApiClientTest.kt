package com.karumi.todoapiclient

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import todoapiclient.TodoApiClient
import todoapiclient.dto.TaskDto
import todoapiclient.exception.ItemNotFoundError
import todoapiclient.exception.UnknownApiError

class TodoApiClientTest : MockWebServerTest() {

    private lateinit var apiClient: TodoApiClient

    companion object {
        const val TASK_ID = "1"
        val TASK_CREATED = TaskDto("1","2","Finish this kata",false)
    }

    @Before
    override fun setUp() {
        super.setUp()
        val mockWebServerEndpoint = baseEndpoint
        apiClient = TodoApiClient(mockWebServerEndpoint)
    }

    @Test
    fun sendsAcceptAndContentTypeHeaders() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertRequestContainsHeader("Accept", "application/json")
    }

    @Test
    fun sendsGetAllTaskRequestToTheCorrectEndpoint() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertGetRequestSentTo("/todos")
    }

    @Test
    fun parsesTasksProperlyGettingAllTheTasks() {
        enqueueMockResponse(200, "getTasksResponse.json")

        val tasks = apiClient.allTasks.right!!

        assertEquals(200, tasks.size.toLong())
        assertTaskContainsExpectedValues(tasks[0])
    }

    @Test
    fun parseTaskProperlyGettingJustOneTask() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")
        val task = apiClient.getTaskById(TASK_ID).right!!
        assertTaskContainsExpectedValues(task)

    }

    @Test
    fun taskNotFound() {
        enqueueMockResponse(404)
        val error = apiClient.getTaskById(TASK_ID).left
        assertEquals(ItemNotFoundError, error)
    }

    @Test
    fun serverError() {
        enqueueMockResponse(500)

        val error = apiClient.allTasks.left
        assertEquals(UnknownApiError(500), error)
    }

    @Test
    fun getTaskByIdRequestContainsTheIdAsPartOfThePath() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        apiClient.getTaskById(TASK_ID)

        assertGetRequestSentTo("/todos/$TASK_ID")
    }

    @Test
    fun sendsAcceptAndContentTypeHeaderById() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        apiClient.getTaskById(TASK_ID)

        assertRequestContainsHeader("Accept", "application/json")
    }

    @Test
    fun addTaskRequestContainsCorrectPath() {
        enqueueMockResponse(201, "addTaskResponse.json")

        apiClient.addTask(TASK_CREATED)
        assertPostRequestSentTo("/todos")
    }

    @Test
    fun addTaskRequestContainsExpectedBody() {
        enqueueMockResponse(201, "addTaskResponse.json")

        apiClient.addTask(TASK_CREATED)

        assertRequestBodyEquals("addTaskRequest.json")
    }

    @Test
    fun deleteTaskByIdContainsCorrectPath() {
        enqueueMockResponse()

        apiClient.deleteTaskById(TASK_ID)
        assertDeleteRequestSentTo("/todos/$TASK_ID")
    }

    @Test
    fun deleteTaskNotFoundError() {
        enqueueMockResponse(404)

        val error = apiClient.deleteTaskById(TASK_ID)

        assertEquals(ItemNotFoundError, error)
    }

    private fun assertTaskContainsExpectedValues(task: TaskDto?) {
        assertTrue(task != null)
        assertEquals(task?.id, "1")
        assertEquals(task?.userId, "1")
        assertEquals(task?.title, "delectus aut autem")
        assertFalse(task!!.isFinished)
    }
}
