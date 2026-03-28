from __future__ import annotations

from typing import Generic, Iterator, TypeVar

T = TypeVar("T")


class LinkedList(Generic[T]):

    class ListNode:
        __slots__ = ("value", "next")

        def __init__(self, value: object) -> None:
            self.value = value
            self.next: LinkedList.ListNode | None = None

    def __init__(self) -> None:
        self.head: LinkedList.ListNode | None = None
        self.tail: LinkedList.ListNode | None = None

    def append(self, item: T) -> None:
        node = LinkedList.ListNode(item)
        if self.tail is None:
            self.head = self.tail = node
        else:
            self.tail.next = node
            self.tail = node

    def __iter__(self) -> Iterator[T]:
        n = self.head
        while n is not None:
            yield n.value
            n = n.next
