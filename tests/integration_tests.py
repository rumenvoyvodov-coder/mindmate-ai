import asyncio
import httpx
import pytest
import uuid
import json

BASE_URL = "http://localhost:8000/api/v1"

@pytest.fixture
def unique_email():
    return f"test_{uuid.uuid4()}@example.com"

@pytest.fixture
def password():
    return "testpassword123"

async def test_full_flow(unique_email, password):
    async with httpx.AsyncClient(timeout=30.0) as client:
        # 1. Register
        print("\n1. Testing Registration...")
        reg_data = {
            "email": unique_email,
            "password": password,
            "full_name": "Test User"
        }
        response = await client.post(f"{BASE_URL}/auth/register", json=reg_data)
        assert response.status_code == 200, f"Registration failed: {response.text}"
        user_data = response.json()
        assert user_data["email"] == unique_email
        print("   Success!")

        # 2. Login
        print("2. Testing Login...")
        login_data = {
            "username": unique_email,
            "password": password
        }
        # FastAPI OAuth2 uses form data for login
        response = await client.post(f"{BASE_URL}/auth/login", data=login_data)
        assert response.status_code == 200, f"Login failed: {response.text}"
        token_data = response.json()
        token = token_data["access_token"]
        headers = {"Authorization": f"Bearer {token}"}
        print("   Success!")

        # 3. Get Me
        print("3. Testing Protected Endpoint (/users/me)...")
        response = await client.get(f"{BASE_URL}/users/me", headers=headers)
        assert response.status_code == 200
        assert response.json()["email"] == unique_email
        print("   Success!")

        # 4. Create Conversation
        print("4. Testing Create Conversation...")
        response = await client.post(f"{BASE_URL}/conversations/", headers=headers, json={"title": "Test Chat"})
        assert response.status_code == 200
        conv_id = response.json()["id"]
        print(f"   Success! (ID: {conv_id})")

        # 5. List Conversations
        print("5. Testing List Conversations...")
        response = await client.get(f"{BASE_URL}/conversations/", headers=headers)
        assert response.status_code == 200
        assert any(c["id"] == conv_id for c in response.json())
        print("   Success!")

        # 6. Chat Flow (Standard)
        print("6. Testing Chat (Standard)...")
        chat_data = {
            "messages": [{"role": "user", "content": "Hello, I'm feeling a bit stressed today."}],
            "conversation_id": conv_id
        }
        response = await client.post(f"{BASE_URL}/chat/", headers=headers, json=chat_data)
        # Note: This might fail if the API key is invalid, but we want to see HOW it fails
        if response.status_code == 200:
            chat_res = response.json()
            assert "content" in chat_res
            assert "mood" in chat_res
            print(f"   Success! Response: {chat_res['content'][:50]}...")
        else:
            print(f"   Chat failed (expected if API key missing): {response.status_code} - {response.text}")

        # 7. Usage Tracking
        print("7. Testing Usage Tracking...")
        response = await client.get(f"{BASE_URL}/usage/", headers=headers)
        assert response.status_code == 200
        usage = response.json()
        print(f"   Usage: {usage['messages_sent_today']}/{usage['daily_limit']}")
        
        # 8. Subscription Upgrade
        print("8. Testing Subscription Upgrade...")
        response = await client.post(f"{BASE_URL}/subscribe/premium", headers=headers)
        assert response.status_code == 200
        assert response.json()["tier"] == "premium"
        
        # Verify usage again (should be unlimited)
        response = await client.get(f"{BASE_URL}/usage/", headers=headers)
        usage = response.json()
        assert usage["daily_limit"] is None
        print("   Upgrade Success! (Unlimited messages)")

        # 9. Streaming Chat
        print("9. Testing Streaming Chat...")
        try:
            response = await client.post(f"{BASE_URL}/chat/stream", headers=headers, json=chat_data)
            if response.status_code == 200:
                print("   Streaming started...")
                # We would normally read the stream here, but for now just checking connection
                async for line in response.aiter_lines():
                    if line:
                        print(f"      Stream chunk: {line[:50]}...")
                        break
            else:
                print(f"   Streaming failed (expected if API key missing): {response.status_code} - {response.text}")
        except Exception as e:
            print(f"   Streaming exception (expected if API key missing): {type(e).__name__}: {e}")

        # 10. Delete Conversation
        print("10. Testing Delete Conversation...")
        response = await client.delete(f"{BASE_URL}/conversations/{conv_id}", headers=headers)
        assert response.status_code == 200
        
        # Verify it's gone
        response = await client.get(f"{BASE_URL}/conversations/{conv_id}", headers=headers)
        assert response.status_code == 404
        print("   Success!")

if __name__ == "__main__":
    import asyncio
    asyncio.run(test_full_flow(f"manual_{uuid.uuid4()}@test.com", "password123"))
