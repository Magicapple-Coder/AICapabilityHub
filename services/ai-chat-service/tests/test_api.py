from fastapi.testclient import TestClient

from app.main import app
from app.core.config import settings
import app.service.chat_service as chat_module


client = TestClient(app)


def test_health_has_unified_result() -> None:
    response = client.get("/health")
    body = response.json()
    assert response.status_code == 200
    assert body["code"] == 0
    assert body["data"]["status"] == "UP"
    assert body["requestId"]


def test_mock_completion_does_not_require_downstream_services() -> None:
    response = client.post(
        "/api/chat/completions",
        headers={"X-User-Id": "test-user", "X-Request-Id": "test-request"},
        json={"messages": [{"role": "user", "content": "hello"}]},
    )
    body = response.json()
    assert response.status_code == 200
    assert body["code"] == 0
    assert body["requestId"] == "test-request"
    assert body["data"]["mock"] is True


def test_invalid_completion_uses_business_validation_code() -> None:
    response = client.post(
        "/api/chat/completions",
        headers={"X-User-Id": "test-user"},
        json={"messages": []},
    )
    body = response.json()
    assert response.status_code == 200
    assert body["code"] == 2001
    assert body["data"] is None


def test_internal_chain_forwards_request_id(monkeypatch) -> None:
    calls: list[tuple[str, str, str, dict]] = []

    async def fake_call_internal(service_name, method, path, **kwargs):
        calls.append((service_name, method, path, kwargs))
        return {"available": True}

    monkeypatch.setattr(settings, "enable_internal_chain", True)
    monkeypatch.setattr(chat_module, "call_internal", fake_call_internal)
    response = client.post(
        "/api/chat/completions",
        headers={"X-User-Id": "test-user", "X-Request-Id": "chain-request"},
        json={"messages": [{"role": "user", "content": "hello"}]},
    )
    assert response.status_code == 200
    assert response.json()["code"] == 0
    assert [item[2] for item in calls] == [
        "/internal/user/check",
        "/internal/billing/record",
    ]
    assert calls[1][3]["json"]["request_id"] == "chain-request"
